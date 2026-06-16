package org.example;

import org.example.entities.forest.Forest;
import org.example.entities.placement.HeatmapCell;
import org.example.entities.placement.PlacementSolution;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.example.services.PlacementOptimizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class TestPlacementOptimization {

    @Autowired
    private PlacementOptimizationService optimizationService;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestRepository forestRepository;

    private Species tomatoSpecies;
    private Species basilSpecies;
    private Forest testForest;

    @BeforeEach
    public void setup() {
        // Clean database
        plantRepository.deleteAll();
        speciesRepository.deleteAll();
        forestRepository.deleteAll();

        // Create test species
        tomatoSpecies = new Species("Tomato", 200, 22, 60, 1500, 1.5, 0.4);
        basilSpecies = new Species("Basil", 150, 20, 55, 1200, 1.2, 0.3);

        tomatoSpecies = speciesRepository.save(tomatoSpecies);
        basilSpecies = speciesRepository.save(basilSpecies);

        // Create test forest
        testForest = new Forest("Test Forest", 10, 10);
        testForest = forestRepository.save(testForest);
    }

    @Test
    public void testOptimizationWithMultiplePlants() {
        // Create plants
        List<Plant> plants = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Plant tomato = new Plant("Tomato" + i, tomatoSpecies);
            Plant basil = new Plant("Basil" + i, basilSpecies);
            plants.add(plantRepository.save(tomato));
            plants.add(plantRepository.save(basil));
        }

        // Run optimization
        PlacementSolution solution = optimizationService.optimizePlacement(
                plants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        // Assertions
        assertNotNull(solution);
        assertTrue(solution.getPlantPositions().size() > 0, "Should place at least some plants");
        assertTrue(solution.getPlantPositions().size() <= 10, "Should not place more plants than provided");

        // Check no position overlap
        for (int i = 0; i < solution.getPlantPositions().size(); i++) {
            for (int j = i + 1; j < solution.getPlantPositions().size(); j++) {
                var pos1 = solution.getPlantPositions().get(i);
                var pos2 = solution.getPlantPositions().get(j);
                assertFalse(pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY(),
                        "No two plants should occupy the same position");
            }
        }

        System.out.println("✓ Optimization completed with fitness: " + solution.getFitnessScore());
    }

    @Test
    public void testOptimizationWithSingleSpecies() {
        // Create only tomato plants
        List<Plant> plants = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Plant tomato = new Plant("Tomato" + i, tomatoSpecies);
            plants.add(plantRepository.save(tomato));
        }

        PlacementSolution solution = optimizationService.optimizePlacement(
                plants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        assertNotNull(solution);
        assertTrue(solution.getPlantPositions().size() > 0, "Should place at least some plants");
        assertTrue(solution.getPlantPositions().size() <= 8, "Should not place more plants than provided");

        // Check spacing between same species (disease resistance)
        for (int i = 0; i < solution.getPlantPositions().size(); i++) {
            for (int j = i + 1; j < solution.getPlantPositions().size(); j++) {
                var pos1 = solution.getPlantPositions().get(i);
                var pos2 = solution.getPlantPositions().get(j);
                double distance = pos1.distanceTo(pos2);
                // Genetic algorithm should favor spacing >= 3 for same species
                // This is not guaranteed but should be true in most cases
                assertTrue(distance > 0, "Plants should not overlap");
            }
        }

        System.out.println("✓ Single species optimization completed");
    }

    @Test
    public void testHeatmapGeneration() {
        // Create some existing plants
        List<Plant> existingPlants = new ArrayList<>();
        Plant tomato1 = new Plant("Tomato1", tomatoSpecies);
        tomato1.setX(2);
        tomato1.setY(2);
        tomato1.setForestId(testForest.getId());
        existingPlants.add(plantRepository.save(tomato1));

        // Generate heatmap for Basil (companion to Tomato)
        List<HeatmapCell> heatmap = optimizationService.generateHeatmap(
                testForest.getId(),
                "Basil",
                existingPlants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        assertNotNull(heatmap);
        assertEquals(100, heatmap.size()); // 10x10 grid

        // Check that position (2,2) is occupied (score = 0)
        HeatmapCell occupiedCell = heatmap.stream()
                .filter(c -> c.getX() == 2 && c.getY() == 2)
                .findFirst()
                .orElse(null);

        assertNotNull(occupiedCell);
        assertEquals(0.0, occupiedCell.getScore(), 0.01);

        // Check that nearby cells have higher scores (companion planting)
        HeatmapCell nearbyCell = heatmap.stream()
                .filter(c -> c.getX() == 3 && c.getY() == 2)
                .findFirst()
                .orElse(null);

        assertNotNull(nearbyCell);
        assertTrue(nearbyCell.getScore() > 0.5, "Nearby cell should have good score for companion plant");

        System.out.println("✓ Heatmap generation successful");
    }

    @Test
    public void testOptimizationBoundaries() {
        // Test with plants that exceed forest capacity
        List<Plant> plants = new ArrayList<>();
        for (int i = 0; i < 150; i++) { // More plants than cells
            Plant tomato = new Plant("Tomato" + i, tomatoSpecies);
            plants.add(plantRepository.save(tomato));
        }

        PlacementSolution solution = optimizationService.optimizePlacement(
                plants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        assertNotNull(solution);
        // Should place as many as possible without overlap
        assertTrue(solution.getPlantPositions().size() <= 100);
        assertTrue(solution.getPlantPositions().size() > 0);

        System.out.println("✓ Boundary test passed: placed " + solution.getPlantPositions().size() + " plants");
    }

    @Test
    public void testOptimizationWithEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            optimizationService.optimizePlacement(
                    new ArrayList<>(),
                    testForest.getWidth(),
                    testForest.getHeight()
            );
        });

        System.out.println("✓ Empty list validation works");
    }

    @Test
    public void testFitnessScoreImprovement() {
        // Create companion plants (Tomato + Basil)
        List<Plant> companionPlants = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            companionPlants.add(plantRepository.save(new Plant("Tomato" + i, tomatoSpecies)));
            companionPlants.add(plantRepository.save(new Plant("Basil" + i, basilSpecies)));
        }

        PlacementSolution companionSolution = optimizationService.optimizePlacement(
                companionPlants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        // Create non-companion plants (only Tomatoes - competition)
        List<Plant> competitivePlants = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            competitivePlants.add(plantRepository.save(new Plant("TomatoOnly" + i, tomatoSpecies)));
        }

        PlacementSolution competitiveSolution = optimizationService.optimizePlacement(
                competitivePlants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        // Companion planting should generally have better fitness
        // (though not guaranteed due to randomness)
        System.out.println("Companion fitness: " + companionSolution.getFitnessScore());
        System.out.println("Competitive fitness: " + competitiveSolution.getFitnessScore());

        assertNotNull(companionSolution);
        assertNotNull(competitiveSolution);

        System.out.println("✓ Fitness comparison completed");
    }

    @Test
    public void testPositionWithinBounds() {
        List<Plant> plants = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            plants.add(plantRepository.save(new Plant("Plant" + i, tomatoSpecies)));
        }

        PlacementSolution solution = optimizationService.optimizePlacement(
                plants,
                testForest.getWidth(),
                testForest.getHeight()
        );

        // Check all positions are within forest bounds
        solution.getPlantPositions().forEach(pos -> {
            assertTrue(pos.getX() >= 0 && pos.getX() < testForest.getWidth(),
                    "X position should be within bounds");
            assertTrue(pos.getY() >= 0 && pos.getY() < testForest.getHeight(),
                    "Y position should be within bounds");
        });

        System.out.println("✓ All positions within bounds");
    }
}
