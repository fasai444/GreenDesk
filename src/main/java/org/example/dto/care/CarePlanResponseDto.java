package org.example.dto.care;

import java.time.Instant;
import java.util.List;

/**
 * ============================================================
 * CARE PLAN RESPONSE DTO
 * ============================================================
 *
 * Vue agrégée du plan de soins d’une plante.
 *
 * ============================================================
 */

public class CarePlanResponseDto {

    private String plantId;

    private List<CareTaskResponseDto> tasks;

    private Instant lastRecalculationDate;

    // ============================================================
    // GETTERS / SETTERS
    // ============================================================

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public List<CareTaskResponseDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<CareTaskResponseDto> tasks) {
        this.tasks = tasks;
    }

    public Instant getLastRecalculationDate() {
        return lastRecalculationDate;
    }

    public void setLastRecalculationDate(Instant lastRecalculationDate) {
        this.lastRecalculationDate = lastRecalculationDate;
    }
}