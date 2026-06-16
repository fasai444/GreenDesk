package org.example.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Modèle pour la Feature L3-F2 : Événements appliqués à une forêt entière.
 * Permet de simuler des phénomènes comme des vagues de chaleur ou des tempêtes.
 */
@Document(collection = "stimuli")
public class Stimulus {

    @Id
    private String id;
    
    private String type;           // ex: "HEATWAVE", "RAIN", "STORM" [cite: 151]
    private String forestId;      // ID de la forêt ciblée [cite: 151]
    private double intensity;      // Puissance du stimulus (ex: +10°C) [cite: 151]
    private int durationHours;     // Durée de l'impact [cite: 151]
    private LocalDateTime createdAt;

    public Stimulus() {
        this.createdAt = LocalDateTime.now();
    }

    public Stimulus(String type, String forestId, double intensity, int durationHours) {
        this.type = type;
        this.forestId = forestId;
        this.intensity = intensity;
        this.durationHours = durationHours;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters et Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getForestId() { return forestId; }
    public void setForestId(String forestId) { this.forestId = forestId; }

    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) { this.intensity = intensity; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
}