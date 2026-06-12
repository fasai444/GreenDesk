package org.example.dto.care;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de création automatique d'une tâche de soin via le moteur WNS.
 */
public class CreateTaskRequest {

    @NotBlank
    private String plantId;

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }
}
