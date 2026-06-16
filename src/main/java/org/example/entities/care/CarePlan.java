package org.example.entities.care;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CARE PLAN
 * ============================================================
 *
 * Représente le plan global de soins d'une plante.
 *
 * Contient :
 * - les tâches associées,
 * - la dernière date de recalcul,
 * - le suivi du cycle de soins.
 *
 * Une plante possède un seul CarePlan.
 *
 * ============================================================
 */

@Document(collection = "care_plans")
public class CarePlan {

    @Id
    private String id;

    /**
     * ID unique de la plante concernée
     */
    private String plantId;

    /**
     * Liste des IDs des tâches liées
     */
    private List<String> taskIds = new ArrayList<>();

    /**
     * Dernière recompute globale
     */
    private Instant lastRecalculationDate;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public CarePlan() {
    }

    public CarePlan(String plantId) {
        this.plantId = plantId;
        this.lastRecalculationDate = Instant.now();
    }

    // ============================================================
    // HELPERS METIER
    // ============================================================

    /**
     * Ajoute une tâche au plan
     */
    public void addTask(String taskId) {
        this.taskIds.add(taskId);
    }

    /**
     * Retire une tâche du plan
     */
    public void removeTask(String taskId) {
        this.taskIds.remove(taskId);
    }

    /**
     * Met à jour la date de recalcul
     */
    public void touchRecalculation() {
        this.lastRecalculationDate = Instant.now();
    }

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public String getId() {
        return id;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public Instant getLastRecalculationDate() {
        return lastRecalculationDate;
    }

    public void setLastRecalculationDate(Instant lastRecalculationDate) {
        this.lastRecalculationDate = lastRecalculationDate;
    }
}