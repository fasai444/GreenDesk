package org.example.entities.weather;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.config.ParisLocalDateTimeSerializer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "weather_alerts")
public class WeatherAlert {
    
    @Id
    private String id;
    
    private String eventId;        // ID unique de l'alerte Tomorrow.io
    private String type;           // frost, heatwave, heavy_rain, etc.
    private double[] coords;       // [latitude, longitude]

    @JsonSerialize(using = ParisLocalDateTimeSerializer.class)
    private LocalDateTime timestamp;

    private String severity;       // low, medium, high
    private Map<String, Object> details;
    private boolean processed;

    @JsonSerialize(using = ParisLocalDateTimeSerializer.class)
    private LocalDateTime processedAt;

    private boolean acknowledged;

    @JsonSerialize(using = ParisLocalDateTimeSerializer.class)
    private LocalDateTime acknowledgedAt;

    // Constructeur par défaut (obligatoire pour MongoDB)
    public WeatherAlert() {}

    // Constructeur avec champs obligatoires
    public WeatherAlert(String eventId, String type, double[] coords, 
                        LocalDateTime timestamp, String severity, 
                        Map<String, Object> details) {
        this.eventId = eventId;
        this.type = type;
        this.coords = coords;
        this.timestamp = timestamp;
        this.severity = severity;
        this.details = details;
        this.processed = false;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
}
