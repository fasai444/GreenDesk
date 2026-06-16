package org.example.entities.care;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * ============================================================
 * CARE TASK
 * ============================================================
 *
 * Représente une tâche de soin générée automatiquement
 * par le moteur intelligent du calendrier dynamique.
 *
 * Exemples :
 * - Arrosage
 * - Fertilisation
 * - Taille
 * - Ajustement chauffage
 *
 * Cette entité est au centre du workflow :
 *
 * PENDING -> DONE
 * PENDING -> CANCELED
 *
 * ============================================================
 */

@Document(collection = "care_tasks")

/**
 * Index métier pour :
 * - idempotence
 * - recherche rapide
 * - scheduler
 */
@CompoundIndex(
        name = "plant_type_schedule_idx",
        def = "{'plantId':1, 'type':1, 'scheduledAt':1}"
)
public class CareTask {

    @Id
    private String id;

    /**
     * ID de la plante concernée
     */
    private String plantId;

    /**
     * ID de la forêt
     * utile pour filtres dashboard
     */
    private String forestId;

    /**
     * Type métier de la tâche
     */
    private CareTaskType type;

    /**
     * Description lisible utilisateur
     */
    private String description;

    /**
     * Score WNS ayant déclenché la tâche
     */
    private double wnsScore;

    /**
     * Priorité calculée
     */
    private TaskPriority priority;

    /**
     * Peut être déplacée automatiquement
     * lors d’une alerte météo critique
     */
    private boolean isFlexible = true;

    /**
     * Date prévue d’exécution
     */
    private Instant scheduledAt;

    /**
     * Deadline réelle
     */
    private Instant dueAt;

    /**
     * Etat courant
     */
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Dépendance météo éventuelle
     */
    private WeatherDependency weatherDependency =
            WeatherDependency.NONE;

    /**
     * ID agenda externe
     * (Google Calendar futur)
     */
    private String externalId;

    /**
     * Date création
     */
    private Instant createdAt = Instant.now();

    /**
     * Date clôture
     */
    private Instant closedAt;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public CareTask() {
    }

    public CareTask(String plantId,
                    String forestId,
                    CareTaskType type,
                    String description,
                    double wnsScore,
                    TaskPriority priority,
                    Instant scheduledAt,
                    Instant dueAt) {

        this.plantId = plantId;
        this.forestId = forestId;
        this.type = type;
        this.description = description;
        this.wnsScore = wnsScore;
        this.priority = priority;
        this.scheduledAt = scheduledAt;
        this.dueAt = dueAt;
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

    public String getForestId() {
        return forestId;
    }

    public void setForestId(String forestId) {
        this.forestId = forestId;
    }

    public CareTaskType getType() {
        return type;
    }

    public void setType(CareTaskType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getWnsScore() {
        return wnsScore;
    }

    public void setWnsScore(double wnsScore) {
        this.wnsScore = wnsScore;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public boolean isFlexible() {
        return isFlexible;
    }

    public void setFlexible(boolean flexible) {
        isFlexible = flexible;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public WeatherDependency getWeatherDependency() {
        return weatherDependency;
    }

    public void setWeatherDependency(WeatherDependency weatherDependency) {
        this.weatherDependency = weatherDependency;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}