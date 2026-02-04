package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Cycle de saisons attaché à une forêt.
 * Gère la progression automatique des saisons : hiver → printemps → été → automne.
 */
@Document(collection = "season_cycles")
public class SeasonCycle {
    
    @Id
    private String id;
    
    private String forestId;
    private SeasonType currentSeason;
    private int monthsInCurrentSeason;  // Nombre de mois écoulés dans la saison actuelle
    private int monthsPerSeason;        // Durée d'une saison en mois (par défaut 3)
    private LocalDateTime lastUpdate;
    private boolean active;
    
    public SeasonCycle(String forestId) {
        this.forestId = forestId;
        this.currentSeason = SeasonType.SPRING; // Commence au printemps
        this.monthsInCurrentSeason = 0;
        this.monthsPerSeason = 3; // 3 mois par saison
        this.lastUpdate = LocalDateTime.now();
        this.active = true;
    }
    
    // Constructeur par défaut pour MongoDB
    protected SeasonCycle() {
        this.monthsPerSeason = 3;
    }
    
    /**
     * Avance le cycle des saisons.
     * Simule le passage du temps (en mois).
     */
    public void advanceTime(int monthsElapsed) {
        if (!active) return;
        
        monthsInCurrentSeason += monthsElapsed;
        
        // Si on a dépassé la durée de la saison, passer à la suivante
        while (monthsInCurrentSeason >= monthsPerSeason) {
            monthsInCurrentSeason -= monthsPerSeason;
            currentSeason = currentSeason.next();
        }
        
        lastUpdate = LocalDateTime.now();
    }
    
    /**
     * Obtient la saison actuelle avec ses modificateurs.
     */
    public Season getCurrentSeasonData() {
        return Season.getByType(currentSeason);
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public String getForestId() {
        return forestId;
    }
    
    public void setForestId(String forestId) {
        this.forestId = forestId;
    }
    
    public SeasonType getCurrentSeason() {
        return currentSeason;
    }
    
    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }
    
    public int getMonthsInCurrentSeason() {
        return monthsInCurrentSeason;
    }
    
    public void setMonthsInCurrentSeason(int monthsInCurrentSeason) {
        this.monthsInCurrentSeason = monthsInCurrentSeason;
    }
    
    public int getMonthsPerSeason() {
        return monthsPerSeason;
    }
    
    public void setMonthsPerSeason(int monthsPerSeason) {
        this.monthsPerSeason = monthsPerSeason;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
