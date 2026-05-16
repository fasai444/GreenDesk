package org.example.dto.care;

import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;

import java.time.Instant;

public class CreateManualTaskRequest {

    private String plantId;

    private String plantName;

    private CareTaskType type;

    private String description;

    private TaskPriority priority;

    private Instant dueAt;

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
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

    public TaskPriority getPriority() {
        return priority;
    }
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }
}