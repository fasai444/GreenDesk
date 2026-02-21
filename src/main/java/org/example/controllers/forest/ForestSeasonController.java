package org.example.controllers.forest;

import org.example.entites.environment.Season;
import org.example.entites.environment.SeasonCycle;
import org.example.services.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller pour gérer les cycles de saisons des forêts.
 * Ajout à ForestController pour ne pas le surcharger.
 */
@RestController
@RequestMapping("/api/forests")
public class ForestSeasonController {
    
    @Autowired
    private SeasonService seasonService;
    
    /**
     * POST /api/forests/{forestId}/season-cycle
     * Crée un cycle de saisons pour une forêt.
     */
    @PostMapping("/{forestId}/season-cycle")
    public ResponseEntity<?> createSeasonCycle(@PathVariable String forestId) {
        try {
            SeasonCycle cycle = seasonService.createSeasonCycle(forestId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cycle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/forests/{forestId}/season-cycle
     * Récupère le cycle de saisons d'une forêt.
     */
    @GetMapping("/{forestId}/season-cycle")
    public ResponseEntity<?> getSeasonCycle(@PathVariable String forestId) {
        return seasonService.getSeasonCycle(forestId)
                .map(cycle -> {
                    // Enrichir la réponse avec les données de la saison actuelle
                    Season currentSeason = cycle.getCurrentSeasonData();
                    Map<String, Object> response = Map.of(
                        "cycle", cycle,
                        "currentSeasonData", currentSeason
                    );
                    return ResponseEntity.ok((Object) response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Aucun cycle de saisons trouvé pour cette forêt")));
    }
    
    /**
     * POST /api/forests/{forestId}/season-cycle/advance
     * Fait avancer le cycle des saisons.
     * 
     * Body: { "monthsElapsed": 3 }
     */
    @PostMapping("/{forestId}/season-cycle/advance")
    public ResponseEntity<?> advanceSeasonCycle(
            @PathVariable String forestId,
            @RequestBody Map<String, Object> request) {
        try {
            int monthsElapsed = (int) request.getOrDefault("monthsElapsed", 1);
            SeasonCycle cycle = seasonService.advanceSeasonCycle(forestId, monthsElapsed);
            
            // Retourner le cycle mis à jour avec les données de la saison actuelle
            Season currentSeason = cycle.getCurrentSeasonData();
            Map<String, Object> response = Map.of(
                "cycle", cycle,
                "currentSeasonData", currentSeason
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/forests/{forestId}/season-cycle
     * Supprime le cycle de saisons d'une forêt.
     */
    @DeleteMapping("/{forestId}/season-cycle")
    public ResponseEntity<?> deleteSeasonCycle(@PathVariable String forestId) {
        seasonService.deleteSeasonCycle(forestId);
        return ResponseEntity.ok(Map.of("message", "Cycle de saisons supprimé"));
    }
}
