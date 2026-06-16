package org.example.services.scheduling;

import org.example.dto.care.CareTaskUpdateRequest;
import org.example.dto.care.CreateManualTaskRequest;
import org.example.dto.care.GenerateTasksRequest;
import org.example.entities.care.*;
import org.example.entities.plant.GrowthStage;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.entities.weather.PlantImpact;
import org.example.repositories.CareTaskRepository;
import org.example.repositories.PlantImpactRepository;
import org.example.repositories.PlantRepository;
import org.example.services.calendar.ExternalCalendarService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CareTaskService {

    private final CareTaskRepository careTaskRepository;
    private final ExternalCalendarService externalCalendarService;
    private final WnsCalculator wnsCalculator;
    private final PlantRepository plantRepository;
    private final PlantImpactRepository plantImpactRepository;
    private final CarePlanService carePlanService;

    public CareTaskService(
            CareTaskRepository careTaskRepository,
            ExternalCalendarService externalCalendarService,
            WnsCalculator wnsCalculator,
            PlantRepository plantRepository,
            PlantImpactRepository plantImpactRepository,
            CarePlanService carePlanService
    ) {
        this.careTaskRepository = careTaskRepository;
        this.externalCalendarService = externalCalendarService;
        this.wnsCalculator = wnsCalculator;
        this.plantRepository = plantRepository;
        this.plantImpactRepository = plantImpactRepository;
        this.carePlanService = carePlanService;
    }

    public CareTask generateTask(Plant plant) {
        List<PlantImpact> history = plantImpactRepository
                .findByPlantIdOrderByTimestampDesc(plant.getId());

        WnsResult wnsResult = wnsCalculator.calculate(plant, history);

        if (wnsResult.getScore() <= WnsResult.THRESHOLD) {
            return null;
        }

        CareTaskType type = determineTaskType(plant, wnsResult);

        if (wnsResult.isSkipWatering() && type == CareTaskType.WATERING) {
            return null;
        }
        Instant scheduledAt = Instant.now().truncatedTo(ChronoUnit.HOURS);

        Optional<CareTask> existing = careTaskRepository
                .findByPlantIdAndTypeAndStatus(plant.getId(), type, TaskStatus.PENDING);

        if (existing.isPresent()) {
            return existing.get();
        }

        CareTask task = new CareTask();
        task.setPlantId(plant.getId());
        task.setForestId(plant.getForestId());
        task.setType(type);
        task.setDescription(buildDescription(type, plant));
        task.setWnsScore(wnsResult.getScore());
        task.setPriority(determinePriority(history));
        task.setFlexible(type == CareTaskType.WATERING || type == CareTaskType.FERTILIZATION);
        task.setScheduledAt(scheduledAt);
        task.setDueAt(scheduledAt.plus(4, ChronoUnit.HOURS));
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());

        if (wnsResult.isRainWithin6Hours() && type == CareTaskType.WATERING) {
            task.setWeatherDependency(WeatherDependency.RAIN_AVOIDED);
        }

        String externalId = externalCalendarService.push(task);
        task.setExternalId(externalId);

        CareTask saved = careTaskRepository.save(task);
        carePlanService.addTaskToPlan(plant.getId(), saved.getId());
        return saved;
    }

    public CareTask createTaskForPlant(String plantId) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));
        CareTask task = generateTask(plant);
        if (task == null) {
            throw new IllegalStateException(
                    "WNS insuffisant ou pluie prévue : aucune tâche requise pour la plante " + plantId
            );
        }
        return task;
    }

    public List<CareTask> generateTasksForRequest(GenerateTasksRequest request) {
        List<Plant> plants = resolvePlants(request);
        List<CareTask> created = new ArrayList<>();

        for (Plant plant : plants) {
            CareTask task = generateTask(plant);
            if (task != null) {
                created.add(task);
            }
        }
        return created;
    }

    public CareTask markAsDone(String taskId) {
        return validateTask(taskId);
    }

    public CareTask validateTask(String taskId) {
        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getStatus() == TaskStatus.DONE) {
            return task;
        }
        requirePending(task, "validée");

        task.setStatus(TaskStatus.DONE);
        task.setClosedAt(Instant.now());
        careTaskRepository.save(task);

        updatePlantHealthAfterCare(task);
        return task;
    }

    public CareTask patchFlexibleTask(String taskId, CareTaskUpdateRequest request) {
        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.isFlexible()) {
            throw new IllegalStateException("Cette tâche n'est pas flexible et ne peut pas être déplacée");
        }
        requirePending(task, "déplacée");

        Instant scheduledAt = request.getScheduledAt() != null
                ? request.getScheduledAt()
                : task.getScheduledAt();
        Instant dueAt = request.getDueAt() != null
                ? request.getDueAt()
                : task.getDueAt();
        if (scheduledAt != null && dueAt != null && dueAt.isBefore(scheduledAt)) {
            throw new IllegalArgumentException("La date d'échéance doit être postérieure à la planification");
        }

        if (request.getScheduledAt() != null) {
            task.setScheduledAt(request.getScheduledAt());
        }
        if (request.getDueAt() != null) {
            task.setDueAt(request.getDueAt());
        }
        if (request.getIsFlexible() != null) {
            task.setFlexible(request.getIsFlexible());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        CareTask saved = careTaskRepository.save(task);
        if (saved.getExternalId() != null) {
            externalCalendarService.update(saved.getExternalId(), saved);
        }
        return saved;
    }

    public void cancelTask(String taskId) {
        CareTask task = careTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        requirePending(task, "annulée");

        task.setStatus(TaskStatus.CANCELED);
        task.setClosedAt(Instant.now());
        careTaskRepository.save(task);

        if (task.getExternalId() != null) {
            externalCalendarService.remove(task.getExternalId());
        }
    }

    public List<CareTask> getAllTasks() {
        return careTaskRepository.findAllByOrderByPriorityDescDueAtAsc();
    }

    public CareTask createManualTask(CreateManualTaskRequest request) {
        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found: " + request.getPlantId()));

        CareTask task = new CareTask();
        task.setPlantId(request.getPlantId());
        task.setForestId(plant.getForestId());
        task.setType(request.getType());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setScheduledAt(Instant.now());
        task.setDueAt(request.getDueAt());
        task.setStatus(TaskStatus.PENDING);
        task.setFlexible(true);
        task.setCreatedAt(Instant.now());
        task.setWnsScore(0.95);

        String externalId = externalCalendarService.push(task);
        task.setExternalId(externalId);

        CareTask saved = careTaskRepository.save(task);
        if (request.getPlantId() != null) {
            carePlanService.addTaskToPlan(request.getPlantId(), saved.getId());
        }
        return saved;
    }

    private void requirePending(CareTask task, String action) {
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException(
                    "Seule une tâche en attente peut être " + action + " (statut actuel : " + task.getStatus() + ")"
            );
        }
    }

    private List<Plant> resolvePlants(GenerateTasksRequest request) {
        if (request.getPlantId() != null) {
            return plantRepository.findById(request.getPlantId())
                    .map(List::of)
                    .orElse(List.of());
        }
        if (request.getForestId() != null) {
            return plantRepository.findByForestId(request.getForestId());
        }
        return plantRepository.findAll();
    }

    private CareTaskType determineTaskType(Plant plant, WnsResult wnsResult) {
        Species species = plant.getSpecies();
        if (species != null && plant.getWaterLevel() < species.getOptimalWaterNeeds() * 0.7) {
            return CareTaskType.WATERING;
        }

        double stress = plant.getStressIndex();
        if (stress > 1.0) {
            stress = stress / 100.0;
        }

        if (stress > 0.6 || wnsResult.getScore() > 0.85) {
            return CareTaskType.WATERING;
        }

        GrowthStage stage = plant.getGrowthStage();
        if (stage == GrowthStage.FLOWERING || stage == GrowthStage.FRUITING) {
            return CareTaskType.FERTILIZATION;
        }

        if (stage == GrowthStage.MATURE) {
            return CareTaskType.PRUNING;
        }

        return CareTaskType.FERTILIZATION;
    }

    private TaskPriority determinePriority(List<PlantImpact> history) {
        if (history == null || history.isEmpty()) {
            return TaskPriority.MEDIUM;
        }

        double sps = history.get(0).getSps();
        if (sps >= 0.8) {
            return TaskPriority.CRITICAL;
        }
        if (sps >= 0.6) {
            return TaskPriority.HIGH;
        }
        if (sps >= 0.4) {
            return TaskPriority.MEDIUM;
        }
        return TaskPriority.LOW;
    }

    private String buildDescription(CareTaskType type, Plant plant) {
        String plantLabel = plant.getName() != null ? plant.getName() : "Plante";
        String plantRef = plantLabel + " #" + plant.getId();

        return switch (type) {
            case WATERING -> {
                int liters = computeWaterLiters(plant);
                yield "Arrosage : " + liters + "L requis pour " + plantRef;
            }
            case FERTILIZATION -> "Fertilisation requise pour " + plantRef;
            case PRUNING -> "Taille requise pour " + plantRef;
            case HEATING_ADJUSTMENT -> "Ajustement chauffage requis pour " + plantRef;
        };
    }

    private int computeWaterLiters(Plant plant) {
        Species species = plant.getSpecies();
        double optimalMl = species != null ? species.getOptimalWaterNeeds() : 2000.0;
        double tailleFactor = wnsCalculator.computeTailleFactor(plant);
        double waterMl = optimalMl * (0.5 + tailleFactor);
        return Math.max(1, (int) Math.ceil(waterMl / 1000.0));
    }

    private void updatePlantHealthAfterCare(CareTask task) {
        plantRepository.findById(task.getPlantId()).ifPresent(plant -> {
            Species species = plant.getSpecies();
            double optimalWater = species != null ? species.getOptimalWaterNeeds() : 200.0;

            switch (task.getType()) {
                case WATERING -> {
                    plant.setWaterLevel(Math.min(optimalWater * 1.2, plant.getWaterLevel() + optimalWater * 0.5));
                    plant.setStressIndex(Math.max(0.0, plant.getStressIndex() - 0.25));
                }
                case FERTILIZATION -> plant.setStressIndex(Math.max(0.0, plant.getStressIndex() - 0.15));
                case PRUNING -> {
                    plant.setStressIndex(Math.max(0.0, plant.getStressIndex() - 0.1));
                    if (plant.getHeightCm() > 10) {
                        plant.setHeightCm(plant.getHeightCm() * 0.95);
                    }
                }
                case HEATING_ADJUSTMENT -> {
                    plant.setTemperature(plant.getTemperature() + 2.0);
                    plant.setStressIndex(Math.max(0.0, plant.getStressIndex() - 0.2));
                }
                default -> { }
            }

            PlantState newState = plant.evaluateState();
            plant.setPlantState(newState);
            plantRepository.save(plant);
        });
    }
}
