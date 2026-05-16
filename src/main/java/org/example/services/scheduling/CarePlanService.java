package org.example.services.scheduling;

import org.example.entities.care.CarePlan;
import org.example.repositories.CarePlanRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarePlanService {

    private final CarePlanRepository carePlanRepository;

    public CarePlanService(CarePlanRepository carePlanRepository) {
        this.carePlanRepository = carePlanRepository;
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
     * Force le recalcul global (Simulé pour l'ADMIN)
     */
    public void recomputeGlobalPlan(String forestId, String plantId) {
        if (plantId != null) {
            CarePlan plan = getOrCreatePlan(plantId);
            plan.touchRecalculation(); // Utilisation de ton helper
            carePlanRepository.save(plan);
        }
    }
}