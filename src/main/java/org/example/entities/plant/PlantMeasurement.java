package org.example.entities.plant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "plant_measurements")
public class PlantMeasurement {
    @Id
    private String id;
    private String plantId;
    private LocalDateTime timestamp;
    private Double measuredHeightCm;   // hauteur mesurée (cm)
    private Double waterAddedMl;       // eau apportée (mL)
    private String observedState;      // HEALTHY, STRESSED, etc. (observation utilisateur)
    private String notes;              // commentaire libre

    public PlantMeasurement() {}

    public PlantMeasurement(String plantId, Double measuredHeightCm, Double waterAddedMl, String observedState, String notes) {
        this.plantId = plantId;
        this.timestamp = LocalDateTime.now();
        this.measuredHeightCm = measuredHeightCm;
        this.waterAddedMl = waterAddedMl;
        this.observedState = observedState;
        this.notes = notes;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Double getMeasuredHeightCm() { return measuredHeightCm; }
    public void setMeasuredHeightCm(Double measuredHeightCm) { this.measuredHeightCm = measuredHeightCm; }
    public Double getWaterAddedMl() { return waterAddedMl; }
    public void setWaterAddedMl(Double waterAddedMl) { this.waterAddedMl = waterAddedMl; }
    public String getObservedState() { return observedState; }
    public void setObservedState(String observedState) { this.observedState = observedState; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
