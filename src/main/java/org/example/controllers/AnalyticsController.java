package org.example.controllers;

import org.example.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * GET /api/analytics/global
     * Statistiques globales : total plantes, forêts, taux de santé, stress moyen.
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        return ResponseEntity.ok(analyticsService.getGlobalStats());
    }

    /**
     * GET /api/analytics/stress-distribution
     * Répartition des plantes par état (HEALTHY / STRESSED / DORMANT / DISEASED).
     */
    @GetMapping("/stress-distribution")
    public ResponseEntity<Map<String, Long>> getStressDistribution() {
        return ResponseEntity.ok(analyticsService.getStressDistribution());
    }

    /**
     * GET /api/analytics/forest-ranking
     * Classement des forêts par score de santé global.
     */
    @GetMapping("/forest-ranking")
    public ResponseEntity<List<Map<String, Object>>> getForestRanking() {
        return ResponseEntity.ok(analyticsService.getForestHealthRanking());
    }

    /**
     * GET /api/analytics/species-distribution
     * Nombre de plantes par espèce, trié par fréquence décroissante.
     */
    @GetMapping("/species-distribution")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesDistribution() {
        return ResponseEntity.ok(analyticsService.getSpeciesDistribution());
    }

    /**
     * GET /api/analytics/top-risk?limit=10
     * Plantes les plus à risque (hors HEALTHY), triées par indice de stress décroissant.
     */
    @GetMapping("/top-risk")
    public ResponseEntity<List<Map<String, Object>>> getTopRisk(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopAtRiskPlants(limit));
    }

    /**
     * GET /api/analytics/environmental
     * Moyennes environnementales (temp, eau, humidité, lux) par forêt.
     */
    @GetMapping("/environmental")
    public ResponseEntity<List<Map<String, Object>>> getEnvironmentalAverages() {
        return ResponseEntity.ok(analyticsService.getEnvironmentalAverages());
    }

    /**
     * GET /api/analytics/species-health
     * Santé détaillée par espèce : taux de santé, stress moyen, niveau de risque.
     */
    @GetMapping("/species-health")
    public ResponseEntity<List<Map<String, Object>>> getSpeciesHealthStats() {
        return ResponseEntity.ok(analyticsService.getSpeciesHealthStats());
    }
}
