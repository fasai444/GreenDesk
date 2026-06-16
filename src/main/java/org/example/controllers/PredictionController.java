package org.example.controllers;

import org.example.services.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    /**
     * GET /api/predictions/plant/{plantId}?days=7
     * Retourne les prédictions de stress et de hauteur pour une plante
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<?> getPlantPredictions(
            @PathVariable String plantId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            if (days < 1 || days > 30) {
                return ResponseEntity.badRequest().body(Map.of("error", "days doit être entre 1 et 30"));
            }
            Map<String, Object> predictions = predictionService.predictPlantEvolution(plantId, days);
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
