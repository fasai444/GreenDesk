package org.example.dto.care;

import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;
import org.example.entities.care.TaskStatus;
import org.example.entities.care.WeatherDependency;

import java.time.Instant;
import java.util.Map;

public class CareTaskResponseDto {

    private String id;
    private String plantId;
    private String plantName; // Pratique pour l'affichage frontend sans refaire un appel API
    private String forestId;
    private CareTaskType type;
    private String description;
    private double wnsScore;
    private Map<String, Object> wnsBreakdown; // Objet détaillé demandé au point 6.4 du CDC
    private TaskPriority priority;
    private boolean isFlexible;
    private Instant scheduledAt;
    private Instant dueAt;
    private TaskStatus status;
    private WeatherDependency weatherDependency;
    private String externalId;
    private Instant createdAt;
    private Instant closedAt;

    // Constructeur vide, Getters et Setters
    public CareTaskResponseDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }

    public String getForestId() { return forestId; }
    public void setForestId(String forestId) { this.forestId = forestId; }

    public CareTaskType getType() { return type; }
    public void setType(CareTaskType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getWnsScore() { return wnsScore; }
    public void setWnsScore(double wnsScore) { this.wnsScore = wnsScore; }

    public Map<String, Object> getWnsBreakdown() { return wnsBreakdown; }
    public void setWnsBreakdown(Map<String, Object> wnsBreakdown) { this.wnsBreakdown = wnsBreakdown; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public boolean isFlexible() { return isFlexible; }
    public void setFlexible(boolean flexible) { isFlexible = flexible; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public WeatherDependency getWeatherDependency() { return weatherDependency; }
    public void setWeatherDependency(WeatherDependency weatherDependency) { this.weatherDependency = weatherDependency; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}