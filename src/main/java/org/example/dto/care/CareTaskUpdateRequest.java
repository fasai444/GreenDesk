package org.example.dto.care;

import jakarta.validation.constraints.FutureOrPresent;
import java.time.Instant;

public class CareTaskUpdateRequest {

    @FutureOrPresent(message = "La nouvelle date de planification doit être dans le présent ou le futur")
    private Instant scheduledAt;

    @FutureOrPresent(message = "La date d'échéance doit être dans le présent ou le futur")
    private Instant dueAt;

    private Boolean isFlexible; // Boolean objet pour détecter s'il est présent ou null dans le JSON

    private String description;

    // Getters et Setters
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Instant getDueAt() { return dueAt; }
    public void setDueAt(Instant dueAt) { this.dueAt = dueAt; }

    public Boolean getIsFlexible() { return isFlexible; }
    public void setIsFlexible(Boolean flexible) { isFlexible = flexible; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}