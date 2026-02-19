package org.example.controllers;

import org.example.entites.Plant;
import org.example.services.PlantServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/plants")
public class PlantController {

    @Autowired
    private PlantServices plantServices;

    // --- Créer une plante ---
    @PostMapping("/create")
    public ResponseEntity<Plant> createPlant(
            @RequestParam String name,
            @RequestParam String speciesId,
            @RequestParam(required = false) Double water,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Double humidity,
            @RequestParam(required = false) Double lux
    ) {
        try {
            Plant plant;
            if (water != null && temperature != null && humidity != null && lux != null) {
                plant = plantServices.createPlant(name, speciesId, water, temperature, humidity, lux);
            } else {
                plant = plantServices.createPlant(name, speciesId);
            }
            return ResponseEntity.ok(plant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --- Récupérer toutes les plantes ---
    @GetMapping
    public ResponseEntity<List<Plant>> getAllPlants() {
        return ResponseEntity.ok(plantServices.getAllPlants());
    }

    // --- Récupérer une plante par ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantById(@PathVariable String id) {
        try {
            return plantServices.getPlantById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --- Récupérer l'état d'une plante ---
    @GetMapping("/{id}/state")
    public ResponseEntity<String> getPlantState(@PathVariable String id) {
        try {
            Optional<Plant> optPlant = plantServices.getPlantById(id);
            return optPlant
                    .map(plant -> ResponseEntity.ok(plant.getPlantState().name()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================== AJOUTS LIVRAISON 3 (L3-F2) ==================

    /**
     * GET /plants/{id}/status
     * Rapport détaillé pour comparer l'impact des stimulus sur les clones.
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getDetailedStatus(@PathVariable String id) {
        try {
            // On récupère l'Optional et on extrait la plante
            Optional<Plant> optPlant = plantServices.getPlantById(id);
            
            if (optPlant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Plant plant = optPlant.get();

            // Construction du rapport de comparaison
            Map<String, Object> report = new HashMap<>();
            report.put("id", plant.getId());
            report.put("name", plant.getName());
            report.put("forestId", plant.getForestId());
            report.put("state", plant.getPlantState());
            report.put("stressIndex", plant.getStressIndex());
            report.put("heightCm", plant.getHeightCm());

            // Détails des capteurs pour expliquer la divergence
            report.put("sensors", Map.of(
                "waterLevel", plant.getWaterLevel(),
                "temperature", plant.getTemperature(),
                "humidity", plant.getHumidity(),
                "lux", plant.getLux()
            ));

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ================== AUTRES MÉTHODES ==================

    // --- UPDATE plante (eau, température, humidité, lux) ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlant(
            @PathVariable String id,
            @RequestParam(required = false) Double water,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Double humidity,
            @RequestParam(required = false) Double lux
    ) {
        try {
            Optional<Plant> optPlant = plantServices.getPlantById(id);

            if (optPlant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Plant plant = optPlant.get();

            if (water != null) plant.setWaterLevel(water);
            if (temperature != null) plant.setTemperature(temperature);
            if (humidity != null) plant.setHumidity(humidity);
            if (lux != null) plant.setLux(lux);

            plant.setPlantState(plant.evaluateState());

            return ResponseEntity.ok(plant);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- DELETE une plante par ID ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlant(@PathVariable String id) {
        try {
            plantServices.deletePlantById(id);
            return ResponseEntity.ok("Plante supprimée avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- DELETE toutes les plantes ---
    @DeleteMapping
    public ResponseEntity<?> deleteAllPlants() {
        plantServices.deleteAllPlants();
        return ResponseEntity.ok("Toutes les plantes ont été supprimées");
    }
}