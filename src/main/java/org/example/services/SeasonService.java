package org.example.services;

import org.example.entities.environment.Season;
import org.example.entities.environment.SeasonCycle;
import org.example.repositories.SeasonCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SeasonService {
    
    @Autowired
    private SeasonCycleRepository seasonCycleRepository;
    
    /**
     * Récupère le catalogue de toutes les saisons prédéfinies.
     */
    public List<Season> getAllSeasons() {
        return Arrays.asList(
            Season.getWinter(),
            Season.getSpring(),
            Season.getSummer(),
            Season.getAutumn()
        );
    }
    
    /**
     * Crée un cycle de saisons pour une forêt.
     */
    public SeasonCycle createSeasonCycle(String forestId) {
        // Vérifier si un cycle existe déjà pour cette forêt
        Optional<SeasonCycle> existingCycle = seasonCycleRepository.findByForestId(forestId);
        if (existingCycle.isPresent()) {
            return existingCycle.get();
        }
        
        SeasonCycle cycle = new SeasonCycle(forestId);
        return seasonCycleRepository.save(cycle);
    }
    
    /**
     * Récupère le cycle de saisons d'une forêt.
     */
    public Optional<SeasonCycle> getSeasonCycle(String forestId) {
        return seasonCycleRepository.findByForestId(forestId);
    }
    
    /**
     * Fait avancer le cycle des saisons d'une forêt.
     * @param forestId ID de la forêt
     * @param monthsElapsed Nombre de mois à faire avancer
     */
    public SeasonCycle advanceSeasonCycle(String forestId, int monthsElapsed) throws Exception {
        SeasonCycle cycle = seasonCycleRepository.findByForestId(forestId)
                .orElseThrow(() -> new Exception("Aucun cycle de saisons trouvé pour la forêt " + forestId));
        
        cycle.advanceTime(monthsElapsed);
        return seasonCycleRepository.save(cycle);
    }
    
    /**
     * Obtient la saison actuelle d'une forêt.
     */
    public Season getCurrentSeason(String forestId) throws Exception {
        SeasonCycle cycle = seasonCycleRepository.findByForestId(forestId)
                .orElseThrow(() -> new Exception("Aucun cycle de saisons trouvé pour la forêt " + forestId));
        
        return cycle.getCurrentSeasonData();
    }
    
    /**
     * Active ou désactive le cycle des saisons pour une forêt.
     */
    public SeasonCycle toggleSeasonCycle(String forestId, boolean active) throws Exception {
        SeasonCycle cycle = seasonCycleRepository.findByForestId(forestId)
                .orElseThrow(() -> new Exception("Aucun cycle de saisons trouvé pour la forêt " + forestId));
        
        cycle.setActive(active);
        return seasonCycleRepository.save(cycle);
    }
    
    /**
     * Supprime le cycle de saisons d'une forêt.
     */
    public void deleteSeasonCycle(String forestId) {
        seasonCycleRepository.findByForestId(forestId)
                .ifPresent(cycle -> seasonCycleRepository.delete(cycle));
    }
}
