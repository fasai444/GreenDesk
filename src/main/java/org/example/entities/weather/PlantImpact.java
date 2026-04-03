package org.example.entities.weather;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "plant_impacts")
public class PlantImpact {
    
    @Id
    private String id;
    
    private String alertId;
    private String plantId;
    private double isr;                 // Indice de Stress des Plantes
    private double sps;                 // Score de Priorité des Soins
    private double previousStress;
    private double newStress;
    private String previousState;
    private String newState;
    private List<String> actionsTaken;
    private LocalDateTime timestamp;

    // Constructeur par défaut (obligatoire pour MongoDB)
    public PlantImpact() {}

    // Constructeur avec champs obligatoires
    public PlantImpact(String alertId, String plantId, double isr, double sps,
                       double previousStress, double newStress, String previousState, 
                       String newState, List<String> actionsTaken) {
        this.alertId = alertId;
        this.plantId = plantId;
        this.isr = isr;
        this.sps = sps;
        this.previousStress = previousStress;
        this.newStress = newStress;
        this.previousState = previousState;
        this.newState = newState;
        this.actionsTaken = actionsTaken;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public double getIsr() {
        return isr;
    }

    public void setIsr(double isr) {
        this.isr = isr;
    }

    public double getSps() {
        return sps;
    }

    public void setSps(double sps) {
        this.sps = sps;
    }

    public double getPreviousStress() {
        return previousStress;
    }

    public void setPreviousStress(double previousStress) {
        this.previousStress = previousStress;
    }

    public double getNewStress() {
        return newStress;
    }

    public void setNewStress(double newStress) {
        this.newStress = newStress;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public List<String> getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(List<String> actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}