package org.example.controllers.plant;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantMeasurement;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.entities.Stimulus;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.StimulusRepository;
import org.example.services.ForestService;
import org.example.services.PlantService;
import org.example.services.StimulusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.services.weather.PlantCalibrationService;
import org.example.repositories.PlantRepository;

@RestController
@RequestMapping({ "/plants", "/api/plants" })
public class PlantController {

    @Autowired
    private PlantService plantServices;

    @Autowired
    private StimulusService stimulusService;

    @Autowired
    private ForestService forestService;

    @Autowired
    private PlantEffectRepository plantEffectRepository;

    @Autowired
    private EffectRepository effectRepository;

    @Autowired
    private StimulusRepository stimulusRepository;

    @Autowired
    private PlantCalibrationService calibrationService;

    @Autowired
    private PlantRepository plantRepository;

    // --- Créer une plante ---
    @PostMapping("/create")
    public ResponseEntity<Plant> createPlant(
            @RequestParam String name,
            @RequestParam String speciesId,
            @RequestParam(required = false) Double water,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Double humidity,
            @RequestParam(required = false) Double lux) {
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
            Optional<Plant> optPlant = plantServices.getPlantById(id);

            if (optPlant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(buildDetailedStatusReport(optPlant.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /plants/compare?leftId=...&amp;rightId=...
     * Compare deux plantes (souvent clone vs clone) avec un résumé de divergence.
     */
    @GetMapping("/compare")
    public ResponseEntity<?> comparePlants(
            @RequestParam String leftId,
            @RequestParam String rightId) {
        try {
            Optional<Plant> leftPlantOpt = plantServices.getPlantById(leftId);
            Optional<Plant> rightPlantOpt = plantServices.getPlantById(rightId);

            if (leftPlantOpt.isEmpty() || rightPlantOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Plant leftPlant = leftPlantOpt.get();
            Plant rightPlant = rightPlantOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("left", buildDetailedStatusReport(leftPlant));
            response.put("right", buildDetailedStatusReport(rightPlant));
            response.put("comparison", Map.of(
                    "sameSpecies", leftPlant.getSpecies() != null && rightPlant.getSpecies() != null
                            && leftPlant.getSpecies().getId().equals(rightPlant.getSpecies().getId()),
                    "stateChanged", leftPlant.getPlantState() != rightPlant.getPlantState(),
                    "stressIndexDelta", rightPlant.getStressIndex() - leftPlant.getStressIndex(),
                    "heightCmDelta", rightPlant.getHeightCm() - leftPlant.getHeightCm(),
                    "sensorDelta", Map.of(
                            "waterLevel", rightPlant.getWaterLevel() - leftPlant.getWaterLevel(),
                            "temperature", rightPlant.getTemperature() - leftPlant.getTemperature(),
                            "humidity", rightPlant.getHumidity() - leftPlant.getHumidity(),
                            "lux", rightPlant.getLux() - leftPlant.getLux())));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /plants/{plantId}/clone?forestId=...&amp;x=...&amp;y=...
     * Clone une plante vers une forêt cible à une position donnée.
     */
    @PostMapping("/{plantId}/clone")
    public ResponseEntity<?> clonePlant(
            @PathVariable String plantId,
            @RequestParam(required = false) String forestId,
            @RequestParam(required = false) String targetForestId,
            @RequestParam int x,
            @RequestParam int y) {
        try {
            String target = targetForestId != null ? targetForestId : forestId;
            if (target == null || target.isBlank()) {
                return ResponseEntity.badRequest().body("forestId (ou targetForestId) est requis");
            }

            Plant clone = stimulusService.clonePlantToForest(plantId, target, x, y);
            forestService.addPlantToForest(target, clone.getId(), x, y);
            return ResponseEntity.ok(clone);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
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
            @RequestParam(required = false) Double lux) {
        try {
            Optional<Plant> optPlant = plantServices.getPlantById(id);

            if (optPlant.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Plant plant = optPlant.get();

            if (water != null)
                plant.setWaterLevel(water);
            if (temperature != null)
                plant.setTemperature(temperature);
            if (humidity != null)
                plant.setHumidity(humidity);
            if (lux != null)
                plant.setLux(lux);

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

    private Map<String, Object> buildDetailedStatusReport(Plant plant) {
        Species species = plant.getSpecies();

        double waterStress = 0.0;
        double tempStress = 0.0;
        double humidityStress = 0.0;
        double lightStress = 0.0;

        if (species != null) {
            if (species.getOptimalWaterNeeds() > 0) {
                waterStress = Math.min(1.0, Math.abs(plant.getWaterLevel() - species.getOptimalWaterNeeds())
                        / species.getOptimalWaterNeeds());
            }
            tempStress = Math.min(1.0,
                    Math.abs(plant.getTemperature() - species.getOptimalTemperature()) / 20.0);
            humidityStress = Math.min(1.0,
                    Math.abs(plant.getHumidity() - species.getOptimalHumidity()) / 50.0);
            if (species.getOptimalLuxNeeds() > 0) {
                lightStress = Math.min(1.0,
                        Math.abs(plant.getLux() - species.getOptimalLuxNeeds()) / species.getOptimalLuxNeeds());
            }
        }

        List<Map<String, Object>> activeEffects = plantEffectRepository.findByPlantIdAndActive(plant.getId(), true)
                .stream()
                .map(pe -> Optional.ofNullable(pe.getEffectId())
                        .filter(id -> !id.isBlank())
                        .flatMap(effectRepository::findById)
                        .map(effect -> {
                            Map<String, Object> effectView = new HashMap<>();
                            effectView.put("plantEffectId", pe.getId());
                            effectView.put("effectId", effect.getId());
                            effectView.put("name", effect.getName());
                            effectView.put("durationHours", effect.getDurationHours());
                            effectView.put("startAt", pe.getStartAt());
                            effectView.put("endAt", pe.getEndAt());
                            return effectView;
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        List<Map<String, Object>> latestStimuli = plant.getForestId() == null
                ? List.of()
                : stimulusRepository.findByForestId(plant.getForestId())
                        .stream()
                        .sorted((Stimulus a, Stimulus b) -> {
                            if (a.getCreatedAt() == null && b.getCreatedAt() == null)
                                return 0;
                            if (a.getCreatedAt() == null)
                                return 1;
                            if (b.getCreatedAt() == null)
                                return -1;
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        })
                        .limit(5)
                        .map(st -> {
                            Map<String, Object> stimulusView = new HashMap<>();
                            stimulusView.put("id", st.getId());
                            stimulusView.put("type", st.getType());
                            stimulusView.put("intensity", st.getIntensity());
                            stimulusView.put("durationHours", st.getDurationHours());
                            stimulusView.put("createdAt", st.getCreatedAt());
                            return stimulusView;
                        })
                        .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("id", plant.getId());
        report.put("plantId", plant.getId());
        report.put("name", plant.getName());
        report.put("plantState", plant.getPlantState());
        report.put("stressIndex", plant.getStressIndex());
        report.put("heightCm", plant.getHeightCm());
        report.put("stressDetails", Map.of(
                "waterStress", waterStress,
                "tempStress", tempStress,
                "humidityStress", humidityStress,
                "lightStress", lightStress));
        report.put("sensors", Map.of(
                "waterLevel", plant.getWaterLevel(),
                "temperature", plant.getTemperature(),
                "humidity", plant.getHumidity(),
                "lux", plant.getLux()));
        report.put("activeEffects", activeEffects);
        report.put("latestStimuli", latestStimuli);
        report.put("recentStimuli", latestStimuli);
        Integer plantX = plant.getX();
        Integer plantY = plant.getY();
        int posX = plantX != null ? plantX : -1;
        int posY = plantY != null ? plantY : -1;
        String forestId = plant.getForestId() != null ? plant.getForestId() : "";
        report.put("forestId", forestId);
        report.put("x", posX);
        report.put("y", posY);
        report.put("forest", Map.of(
                "forestId", forestId,
                "position", Map.of(
                        "x", posX,
                        "y", posY)));
        return report;
    }

    // POST /plants/{id}/measure - Saisie manuelle
    @PostMapping("/{id}/measure")
    public ResponseEntity<?> recordManualMeasurement(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        try {
            Double height = body.containsKey("heightCm") ? ((Number) body.get("heightCm")).doubleValue() : null;
            Double water = body.containsKey("waterAddedMl") ? ((Number) body.get("waterAddedMl")).doubleValue() : null;
            String state = (String) body.get("observedState");
            String notes = (String) body.get("notes");

            PlantMeasurement measurement = calibrationService.recordMeasurement(id, height, water, state, notes);
            return ResponseEntity.ok(measurement);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /plants/{id}/measurements - Historique des mesures
    @GetMapping("/{id}/measurements")
    public ResponseEntity<?> getMeasurementHistory(@PathVariable String id) {
        try {
            List<PlantMeasurement> measurements = calibrationService.getMeasurementHistory(id);
            return ResponseEntity.ok(measurements);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/force-state")
    public ResponseEntity<?> forcePlantState(@PathVariable String id, 
                                            @RequestParam double stressIndex,
                                            @RequestParam String plantState,
                                            @RequestParam(required = false) Double waterLevel,
                                            @RequestParam(required = false) Double temperature,
                                            @RequestParam(required = false) Double humidity,
                                            @RequestParam(required = false) Double lux) {
        try {
            Plant plant = plantRepository.findById(id).orElseThrow();
            
            if (waterLevel != null) plant.setWaterLevel(waterLevel);
            if (temperature != null) plant.setTemperature(temperature);
            if (humidity != null) plant.setHumidity(humidity);
            if (lux != null) plant.setLux(lux);
            
            plant.setStressIndex(stressIndex);
            plant.setPlantState(PlantState.valueOf(plantState.toUpperCase()));
            plantRepository.save(plant);
            return ResponseEntity.ok(plant);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
