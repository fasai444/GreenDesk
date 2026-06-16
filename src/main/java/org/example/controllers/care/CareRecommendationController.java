package org.example.controllers.care;

import org.example.services.CareRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class CareRecommendationController {

    @Autowired
    private CareRecommendationService careRecommendationService;

    /**
     * GET /api/recommendations
     * Recommandations pour toutes les plantes, triées par priorité.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRecommendations() {
        return ResponseEntity.ok(careRecommendationService.getRecommendationsForAll());
    }

    /**
     * GET /api/recommendations/plant/{plantId}
     * Recommandations pour une plante spécifique.
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<?> getForPlant(@PathVariable String plantId) {
        try {
            return ResponseEntity.ok(careRecommendationService.getRecommendationsForPlant(plantId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/recommendations/forest/{forestId}
     * Recommandations pour toutes les plantes d'une forêt.
     */
    @GetMapping("/forest/{forestId}")
    public ResponseEntity<List<Map<String, Object>>> getForForest(@PathVariable String forestId) {
        return ResponseEntity.ok(careRecommendationService.getRecommendationsForForest(forestId));
    }
}
