package org.example.services.scheduling;

import org.example.dto.care.GenerateTasksRequest;
import org.example.entities.care.CarePlan;
import org.example.entities.care.CareTask;
import org.example.repositories.CarePlanRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarePlanService {

    private final CarePlanRepository carePlanRepository;
    private final CareTaskService careTaskService;

    public CarePlanService(CarePlanRepository carePlanRepository, @Lazy CareTaskService careTaskService) {
        this.carePlanRepository = carePlanRepository;
        this.careTaskService = careTaskService;
    }

    /**
     * Récupère ou crée le CarePlan d'une plante
     */
    public CarePlan getOrCreatePlan(String plantId) {
        return carePlanRepository.findByPlantId(plantId)
                .orElseGet(() -> carePlanRepository.save(new CarePlan(plantId)));
    }

    /**
     * Ajoute une tâche au plan d'une plante via ton helper métier
     */
    public void addTaskToPlan(String plantId, String taskId) {
        CarePlan plan = getOrCreatePlan(plantId);
        if (!plan.getTaskIds().contains(taskId)) {
            plan.addTask(taskId); // Utilisation de ton helper
            plan.touchRecalculation(); // Utilisation de ton helper
            carePlanRepository.save(plan);
        }
    }

    /**
     * Recalcule le plan de soins : génère les tâches WNS et met à jour le plan.
     */
    public List<CareTask> recomputeGlobalPlan(String forestId, String plantId) {
        GenerateTasksRequest request = new GenerateTasksRequest();
        request.setForestId(forestId);
        request.setPlantId(plantId);

        List<CareTask> tasks = careTaskService.generateTasksForRequest(request);

        if (plantId != null) {
            CarePlan plan = getOrCreatePlan(plantId);
            plan.touchRecalculation();
            carePlanRepository.save(plan);
        } else if (forestId != null) {
            tasks.stream()
                    .map(CareTask::getPlantId)
                    .distinct()
                    .forEach(pid -> {
                        CarePlan plan = getOrCreatePlan(pid);
                        plan.touchRecalculation();
                        carePlanRepository.save(plan);
                    });
        }

        return tasks;
    }
}