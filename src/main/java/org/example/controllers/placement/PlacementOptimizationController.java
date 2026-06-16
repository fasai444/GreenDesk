package org.example.controllers.placement;

import org.example.entities.forest.Forest;
import org.example.entities.placement.HeatmapCell;
import org.example.entities.placement.PlacementSolution;
import org.example.entities.placement.PlantPosition;
import org.example.entities.plant.Plant;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.services.ForestService;
import org.example.services.PlacementOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for plant placement optimization
 */
@RestController
@RequestMapping("/api/placement")
@CrossOrigin(origins = "*")
public class PlacementOptimizationController {

    @Autowired
    private PlacementOptimizationService optimizationService;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private ForestService forestService;

    /**
     * Optimize placement for a list of plants
     * POST /api/placement/optimize
     */
    @PostMapping("/optimize")
    public ResponseEntity<?> optimizePlacement(@RequestBody OptimizationRequest request) {
        try {
            // Get plants
            List<Plant> plants = request.getPlantIds().stream()
                    .map(id -> plantRepository.findById(id).orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            if (plants.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid plants found");
            }

            // Run optimization
            PlacementSolution solution = optimizationService.optimizePlacement(
                    plants,
                    request.getForestWidth(),
                    request.getForestHeight()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("fitnessScore", solution.getFitnessScore());
            response.put("placements", solution.getPlantPositions());
            response.put("message", "Optimization completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Optimize and apply to forest
     * POST /api/placement/optimize-and-apply/{forestId}
     */
    @PostMapping("/optimize-and-apply/{forestId}")
    public ResponseEntity<?> optimizeAndApply(
            @PathVariable String forestId,
            @RequestBody List<String> plantIds) {
        try {
            Forest forest = forestRepository.findById(forestId)
                    .orElseThrow(() -> new RuntimeException("Forest not found"));

            List<Plant> plants = plantIds.stream()
                    .map(id -> plantRepository.findById(id).orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toList());

            if (plants.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid plants found");
            }

            // Run optimization
            PlacementSolution solution = optimizationService.optimizePlacement(
                    plants,
                    forest.getWidth(),
                    forest.getHeight()
            );

            // Apply solution to forest
            for (PlantPosition pos : solution.getPlantPositions()) {
                Plant plant = plantRepository.findById(pos.getPlantId()).orElse(null);
                if (plant != null) {
                    try {
                        forestService.addPlantToForest(forestId, pos.getPlantId(), pos.getX(), pos.getY());
                    } catch (Exception e) {
                        // Skip if position already occupied
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("forestId", forestId);
            response.put("fitnessScore", solution.getFitnessScore());
            response.put("plantsPlaced", solution.getPlantPositions().size());
            response.put("message", "Optimization applied to forest");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate heatmap for optimal placement zones
     * GET /api/placement/heatmap/{forestId}?species=Tomato
     */
    @GetMapping("/heatmap/{forestId}")
    public ResponseEntity<?> getHeatmap(
            @PathVariable String forestId,
            @RequestParam String species) {
        try {
            Forest forest = forestRepository.findById(forestId)
                    .orElseThrow(() -> new RuntimeException("Forest not found"));

            // Get existing plants in forest
            List<Plant> existingPlants = plantRepository.findAll().stream()
                    .filter(p -> forestId.equals(p.getForestId()))
                    .collect(Collectors.toList());

            // Generate heatmap
            List<HeatmapCell> heatmap = optimizationService.generateHeatmap(
                    forestId,
                    species,
                    existingPlants,
                    forest.getWidth(),
                    forest.getHeight()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("forestId", forestId);
            response.put("species", species);
            response.put("width", forest.getWidth());
            response.put("height", forest.getHeight());
            response.put("heatmap", heatmap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get optimal position suggestion for a plant
     * GET /api/placement/suggest/{forestId}?species=Tomato
     */
    @GetMapping("/suggest/{forestId}")
    public ResponseEntity<?> suggestPosition(
            @PathVariable String forestId,
            @RequestParam String species) {
        try {
            Forest forest = forestRepository.findById(forestId)
                    .orElseThrow(() -> new RuntimeException("Forest not found"));

            List<Plant> existingPlants = plantRepository.findAll().stream()
                    .filter(p -> forestId.equals(p.getForestId()))
                    .collect(Collectors.toList());

            List<HeatmapCell> heatmap = optimizationService.generateHeatmap(
                    forestId,
                    species,
                    existingPlants,
                    forest.getWidth(),
                    forest.getHeight()
            );

            // Find best position
            HeatmapCell bestCell = heatmap.stream()
                    .max((a, b) -> Double.compare(a.getScore(), b.getScore()))
                    .orElse(null);

            if (bestCell == null || bestCell.getScore() == 0.0) {
                return ResponseEntity.ok(Map.of("message", "No suitable position found"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("x", bestCell.getX());
            response.put("y", bestCell.getY());
            response.put("score", bestCell.getScore());
            response.put("species", species);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Request DTO
     */
    public static class OptimizationRequest {
        private List<String> plantIds;
        private int forestWidth;
        private int forestHeight;

        public List<String> getPlantIds() {
            return plantIds;
        }

        public void setPlantIds(List<String> plantIds) {
            this.plantIds = plantIds;
        }

        public int getForestWidth() {
            return forestWidth;
        }

        public void setForestWidth(int forestWidth) {
            this.forestWidth = forestWidth;
        }

        public int getForestHeight() {
            return forestHeight;
        }

        public void setForestHeight(int forestHeight) {
            this.forestHeight = forestHeight;
        }
    }
}
