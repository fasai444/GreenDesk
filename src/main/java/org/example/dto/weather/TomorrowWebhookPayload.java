package org.example.dto.weather;

import java.util.Map;

public class TomorrowWebhookPayload {
    
    private String event_id;
    private String type;
    private double[] coords;
    private String timestamp;
    private String severity;
    private Map<String, Object> details;

    // Constructeur par défaut
    public TomorrowWebhookPayload() {}

    // Constructeur avec champs
    public TomorrowWebhookPayload(String event_id, String type, double[] coords,
                                   String timestamp, String severity, Map<String, Object> details) {
        this.event_id = event_id;
        this.type = type;
        this.coords = coords;
        this.timestamp = timestamp;
        this.severity = severity;
        this.details = details;
    }

    // Getters et Setters
    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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
}