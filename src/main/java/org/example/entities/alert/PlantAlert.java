package org.example.entities.alert;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "plant_alerts")
public class PlantAlert {

    @Id
    private String id;

    private String plantId;

    private LocalDateTime createdAt;

    private AlertType type;

    private AlertSeverity severity;

    private String message;

    private boolean acknowledged;

    public PlantAlert() {}

    public PlantAlert(String plantId, LocalDateTime createdAt, AlertType type, AlertSeverity severity, String message) {
        this.plantId = plantId;
        this.createdAt = createdAt;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.acknowledged = false;
    }

    public String getId() { return id; }
    public String getPlantId() { return plantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public AlertType getType() { return type; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public boolean isAcknowledged() { return acknowledged; }

    public void setId(String id) { this.id = id; }
    public void setPlantId(String plantId) { this.plantId = plantId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setType(AlertType type) { this.type = type; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }
    public void setMessage(String message) { this.message = message; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}