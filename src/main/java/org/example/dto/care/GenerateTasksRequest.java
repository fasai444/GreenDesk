package org.example.dto.care;

public class GenerateTasksRequest {

    private String plantId;  // Optionnel : générer uniquement pour cette plante
    private String forestId; // Optionnel : générer pour toute une forêt

    // Getters et Setters
    public String getPlantId() { return plantId; }
    public void setPlantId(String plantId) { this.plantId = plantId; }

    public String getForestId() { return forestId; }
    public void setForestId(String forestId) { this.forestId = forestId; }
}