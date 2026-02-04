package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Représente l'application d'un effet à une plante spécifique.
 * Gère la durée et l'état actif/inactif de l'effet.
 */
@Document(collection = "plant_effects")
public class PlantEffect {
    
    @Id
    private String id;
    
    private String plantId;
    private String effectId;
    
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean active;
    
    public PlantEffect(String plantId, String effectId, LocalDateTime startAt, int durationHours) {
        this.plantId = plantId;
        this.effectId = effectId;
        this.startAt = startAt;
        this.endAt = startAt.plusHours(durationHours);
        this.active = true;
    }
    
    // Constructeur par défaut pour MongoDB
    protected PlantEffect() {}
    
    /**
     * Vérifie si l'effet est encore actif à un moment donné.
     */
    public boolean isActiveAt(LocalDateTime time) {
        return active && time.isAfter(startAt) && time.isBefore(endAt);
    }
    
    /**
     * Désactive l'effet si sa durée est écoulée.
     */
    public void checkAndUpdateStatus(LocalDateTime currentTime) {
        if (currentTime.isAfter(endAt)) {
            this.active = false;
        }
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public String getPlantId() {
        return plantId;
    }
    
    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }
    
    public String getEffectId() {
        return effectId;
    }
    
    public void setEffectId(String effectId) {
        this.effectId = effectId;
    }
    
    public LocalDateTime getStartAt() {
        return startAt;
    }
    
    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }
    
    public LocalDateTime getEndAt() {
        return endAt;
    }
    
    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
