package org.example.services.scheduling;

import org.example.dto.care.CreateManualTaskRequest;
import org.example.dto.care.GenerateTasksRequest;
import org.example.entities.care.*;
import org.example.entities.plant.Plant;
import org.example.repositories.CareTaskRepository;
import org.example.services.calendar.ExternalCalendarService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * CARE TASK SERVICE
 * ============================================================
 *
 * Service principal de gestion des tâches :
 *
 * - génération automatique
 * - idempotence
 * - push agenda Google
 * - cycle de vie
 * - orchestration métier
 *
 * ============================================================
 */

@Service
public class CareTaskService {

    private final CareTaskRepository careTaskRepository;
    private final ExternalCalendarService externalCalendarService;

    public CareTaskService(
            CareTaskRepository careTaskRepository,
            ExternalCalendarService externalCalendarService
    ) {
        this.careTaskRepository = careTaskRepository;
        this.externalCalendarService = externalCalendarService;
    }

    /**
     * ============================================================
     * Génération simple d’une tâche
     * ============================================================
     */
    public CareTask generateTask(Plant plant) {

        // --------------------------------------------------------
        // TEMPORAIRE :
        // simulera le WNS fourni plus tard par la Personne B
        // --------------------------------------------------------

        double simulatedWns = plant.getStressIndex() / 100.0;

        // seuil métier
        if (simulatedWns < 0.8) {
            return null;
        }

        CareTaskType type = determineTaskType(plant);

        Instant scheduledAt = Instant.now()
                .truncatedTo(ChronoUnit.HOURS);

        // --------------------------------------------------------
        // IDEMPOTENCE
        // --------------------------------------------------------

        Optional<CareTask> existing =
                careTaskRepository
                        .findByPlantIdAndTypeAndScheduledAt(
                                plant.getId(),
                                type,
                                scheduledAt
                        );

        if (existing.isPresent()) {
            return existing.get();
        }

        // --------------------------------------------------------
        // Création tâche
        // --------------------------------------------------------

        CareTask task = new CareTask();

        task.setPlantId(plant.getId());
        task.setForestId(plant.getForestId());

        task.setType(type);

        task.setDescription(
                buildDescription(type, plant)
        );

        task.setWnsScore(simulatedWns);

        task.setPriority(TaskPriority.HIGH);

        task.setFlexible(
                type == CareTaskType.WATERING
                        || type == CareTaskType.FERTILIZATION
        );

        task.setScheduledAt(scheduledAt);

        task.setDueAt(
                scheduledAt.plus(4, ChronoUnit.HOURS)
        );

        task.setStatus(TaskStatus.PENDING);

        task.setCreatedAt(Instant.now());

        // --------------------------------------------------------
        // PUSH GOOGLE CALENDAR
        // --------------------------------------------------------

        String externalId =
                externalCalendarService.push(task);

        task.setExternalId(externalId);

        // --------------------------------------------------------
        // SAVE
        // --------------------------------------------------------

        return careTaskRepository.save(task);
    }

    public List<CareTask> generateTasksForRequest(GenerateTasksRequest request) {
        // En attendant que la personne B déploie la recherche complète,
        // on simule une récupération de Plant via l'id si fourni
        if (request.getPlantId() != null) {
            Plant mockPlant = new Plant();
            mockPlant.setId(request.getPlantId());
            // Appelle ton ancienne méthode qui génère une tâche
            CareTask singleTask = this.generateTask(mockPlant);
            return Collections.singletonList(singleTask);
        }
        return Collections.emptyList();
    }

    /**
     * ============================================================
     * DONE
     * ============================================================
     */
    public CareTask markAsDone(String taskId) {

        CareTask task = careTaskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );

        task.setStatus(TaskStatus.DONE);

        task.setClosedAt(Instant.now());

        return careTaskRepository.save(task);
    }

    /**
     * ============================================================
     * CANCEL
     * ============================================================
     */
    public void cancelTask(String taskId) {

        CareTask task = careTaskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found")
                );

        task.setStatus(TaskStatus.CANCELED);

        careTaskRepository.save(task);

        // suppression agenda externe
        if (task.getExternalId() != null) {
            externalCalendarService.remove(
                    task.getExternalId()
            );
        }
    }

    /**
     * ============================================================
     * Dashboard principal
     * ============================================================
     */
    public List<CareTask> getAllTasks() {

        return careTaskRepository
                .findAllByOrderByPriorityDescDueAtAsc();
    }

    /**
     * ============================================================
     * Détermine type de tâche
     * ============================================================
     */
    private CareTaskType determineTaskType(Plant plant) {

        if (plant.getStressIndex() > 80) {
            return CareTaskType.WATERING;
        }

        return CareTaskType.FERTILIZATION;
    }

    /**
     * ============================================================
     * Description métier
     * ============================================================
     */
    private String buildDescription(
            CareTaskType type,
            Plant plant
    ) {

        return switch (type) {

            case WATERING ->
                    "Watering required for "
                            + plant.getName();

            case FERTILIZATION ->
                    "Fertilization required for "
                            + plant.getName();

            case PRUNING ->
                    "Pruning required for "
                            + plant.getName();

            case HEATING_ADJUSTMENT ->
                    "Heating adjustment required for "
                            + plant.getName();
        };
    }

    public CareTask createManualTask(CreateManualTaskRequest request) {

        CareTask task = new CareTask();

        task.setPlantId(request.getPlantId());

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

        return careTaskRepository.save(task);
    }
}