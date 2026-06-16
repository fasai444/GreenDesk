package org.example.controllers;

import org.example.services.WhatIfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatif")
public class WhatIfController {

    @Autowired
    private WhatIfService whatIfService;

    /**
     * GET /api/whatif/scenarios
     * Liste des scénarios prédéfinis.
     */
    @GetMapping("/scenarios")
    public ResponseEntity<List<Map<String, Object>>> getScenarios() {
        return ResponseEntity.ok(whatIfService.getPredefinedScenarios());
    }

    /**
     * POST /api/whatif/simulate
     * Lance une simulation hypothétique sur une forêt.
     *
     * Body: {
     *   "forestId": "...",
     *   "tempDelta": 12.0,
     *   "waterDelta": 0,
     *   "humidityDelta": -20,
     *   "luxDelta": 3000,
     *   "ticks": 3
     * }
     */
    @PostMapping("/simulate")
    public ResponseEntity<?> simulate(@RequestBody Map<String, Object> body) {
        try {
            String forestId      = (String) body.get("forestId");
            double tempDelta     = toDouble(body.getOrDefault("tempDelta",     0));
            double waterDelta    = toDouble(body.getOrDefault("waterDelta",    0));
            double humidityDelta = toDouble(body.getOrDefault("humidityDelta", 0));
            double luxDelta      = toDouble(body.getOrDefault("luxDelta",      0));
            int    ticks         = toInt(body.getOrDefault("ticks", 1));

            if (forestId == null || forestId.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "forestId requis"));
            if (ticks < 1 || ticks > 20)
                return ResponseEntity.badRequest().body(Map.of("error", "ticks doit être entre 1 et 20"));

            Map<String, Object> result = whatIfService.simulate(
                    forestId, tempDelta, waterDelta, humidityDelta, luxDelta, ticks);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }

    private int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 1; }
    }
}
