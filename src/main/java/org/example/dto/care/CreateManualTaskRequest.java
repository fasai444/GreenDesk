package org.example.dto.care;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;

import java.time.Instant;

public class CreateManualTaskRequest {

    @NotBlank
    private String plantId;

    private String plantName;

    @NotNull
    private CareTaskType type;

    @NotBlank
    private String description;

    @NotNull
    private TaskPriority priority;

    @NotNull
    @Future
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
