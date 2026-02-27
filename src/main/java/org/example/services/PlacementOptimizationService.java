package org.example.services;

import org.example.entities.placement.*;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementing genetic algorithm for optimal plant placement
 */
@Service
public class PlacementOptimizationService {

    // Genetic algorithm parameters
    private static final int POPULATION_SIZE = 100;
    private static final int MAX_GENERATIONS = 200;
    private static final double MUTATION_RATE = 0.15;
    private static final double CROSSOVER_RATE = 0.7;
    private static final int ELITE_COUNT = 5;

    // Compatibility matrix for common plant species
    private final Map<String, Map<String, Double>> compatibilityMatrix;

    public PlacementOptimizationService() {
        this.compatibilityMatrix = initializeCompatibilityMatrix();
    }

    /**
     * Initialize species compatibility matrix
     * Positive values = beneficial companion planting
     * Negative values = competitive/incompatible
     */
    private Map<String, Map<String, Double>> initializeCompatibilityMatrix() {
        Map<String, Map<String, Double>> matrix = new HashMap<>();

        // Tomato relationships
        Map<String, Double> tomatoCompat = new HashMap<>();
        tomatoCompat.put("Basil", 0.8);      // Excellent companion
        tomatoCompat.put("Carrot", 0.5);     // Good companion
        tomatoCompat.put("Potato", -0.7);    // Bad companion (disease spread)
        tomatoCompat.put("Tomato", -0.3);    // Same species - moderate competition
        matrix.put("Tomato", tomatoCompat);

        // Basil relationships
        Map<String, Double> basilCompat = new HashMap<>();
        basilCompat.put("Tomato", 0.8);
        basilCompat.put("Pepper", 0.6);
        basilCompat.put("Basil", -0.2);
        matrix.put("Basil", basilCompat);

        // Carrot relationships
        Map<String, Double> carrotCompat = new HashMap<>();
        carrotCompat.put("Tomato", 0.5);
        carrotCompat.put("Onion", 0.7);
        carrotCompat.put("Carrot", -0.4);
        matrix.put("Carrot", carrotCompat);

        return matrix;
    }

    /**
     * Get compatibility score between two species
     */
    private double getCompatibilityScore(String speciesA, String speciesB) {
        if (speciesA.equals(speciesB)) {
            return -0.3; // Same species competition
        }

        if (compatibilityMatrix.containsKey(speciesA)) {
            return compatibilityMatrix.get(speciesA).getOrDefault(speciesB, 0.0);
        }

        return 0.0; // Neutral if not in matrix
    }

    /**
     * Main optimization method using genetic algorithm
     */
    public PlacementSolution optimizePlacement(List<Plant> plants, int forestWidth, int forestHeight) {
        if (plants == null || plants.isEmpty()) {
            throw new IllegalArgumentException("Plants list cannot be empty");
        }

        // Initialize population
        List<PlacementSolution> population = initializePopulation(plants, forestWidth, forestHeight);

        // Evolve population
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // Evaluate fitness
            population.forEach(this::calculateFitness);

            // Sort by fitness
            Collections.sort(population);

            // Check convergence
            if (generation % 50 == 0) {
                System.out.println("Generation " + generation + " - Best fitness: " + population.get(0).getFitnessScore());
            }

            // Create next generation
            population = evolvePopulation(population);
        }

        // Final evaluation
        population.forEach(this::calculateFitness);
        Collections.sort(population);

        return population.get(0); // Return best solution
    }

    /**
     * Initialize random population
     */
    private List<PlacementSolution> initializePopulation(List<Plant> plants, int width, int height) {
        List<PlacementSolution> population = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            PlacementSolution solution = new PlacementSolution(width, height);
            List<Plant> shuffledPlants = new ArrayList<>(plants);
            Collections.shuffle(shuffledPlants);

            for (Plant plant : shuffledPlants) {
                int attempts = 0;
                int x, y;

                // Find unoccupied position
                do {
                    x = random.nextInt(width);
                    y = random.nextInt(height);
                    attempts++;
                } while (solution.isPositionOccupied(x, y) && attempts < 100);

                if (attempts < 100) {
                    PlantPosition pos = new PlantPosition(
                            plant.getId(),
                            plant.getSpecies().getId(),
                            plant.getSpecies().getName(),
                            x, y
                    );
                    solution.addPlantPosition(pos);
                }
            }

            population.add(solution);
        }

        return population;
    }

    /**
     * Calculate fitness score for a solution
     * Higher score = better placement
     */
    private void calculateFitness(PlacementSolution solution) {
        double fitness = 0.0;
        List<PlantPosition> positions = solution.getPlantPositions();

        for (int i = 0; i < positions.size(); i++) {
            PlantPosition plantA = positions.get(i);

            for (int j = i + 1; j < positions.size(); j++) {
                PlantPosition plantB = positions.get(j);
                double distance = plantA.distanceTo(plantB);

                // 1. Companion planting score (nearby plants)
                if (distance <= 3.0) {
                    double compatScore = getCompatibilityScore(plantA.getSpeciesName(), plantB.getSpeciesName());
                    fitness += compatScore * (4.0 - distance); // Closer = more impact
                }

                // 2. Disease resistance spacing (same species)
                if (plantA.getSpeciesName().equals(plantB.getSpeciesName())) {
                    if (distance < 3.0) {
                        fitness -= (3.0 - distance) * 2.0; // Penalty for too close
                    } else if (distance >= 3.0 && distance <= 5.0) {
                        fitness += 1.0; // Reward good spacing
                    }
                }

                // 3. Resource competition (water/light)
                if (distance < 2.0 && distance > 0) {
                    fitness -= (2.0 - distance) * 0.5; // Penalty for resource competition
                }
            }

            // 4. Edge penalty (prefer central placement for better microclimate)
            int distFromEdge = Math.min(
                    Math.min(plantA.getX(), solution.getForestWidth() - plantA.getX() - 1),
                    Math.min(plantA.getY(), solution.getForestHeight() - plantA.getY() - 1)
            );
            fitness += distFromEdge * 0.1;
        }

        solution.setFitnessScore(fitness);
    }

    /**
     * Evolve population to next generation
     */
    private List<PlacementSolution> evolvePopulation(List<PlacementSolution> population) {
        List<PlacementSolution> newPopulation = new ArrayList<>();

        // Elitism: keep best solutions
        for (int i = 0; i < ELITE_COUNT && i < population.size(); i++) {
            newPopulation.add(new PlacementSolution(population.get(i)));
        }

        Random random = new Random();

        // Generate rest through crossover and mutation
        while (newPopulation.size() < POPULATION_SIZE) {
            PlacementSolution parent1 = tournamentSelection(population);
            PlacementSolution parent2 = tournamentSelection(population);

            PlacementSolution offspring;
            if (random.nextDouble() < CROSSOVER_RATE) {
                offspring = crossover(parent1, parent2);
            } else {
                offspring = new PlacementSolution(parent1);
            }

            if (random.nextDouble() < MUTATION_RATE) {
                mutate(offspring);
            }

            newPopulation.add(offspring);
        }

        return newPopulation;
    }

    /**
     * Tournament selection
     */
    private PlacementSolution tournamentSelection(List<PlacementSolution> population) {
        Random random = new Random();
        int tournamentSize = 5;

        PlacementSolution best = population.get(random.nextInt(population.size()));
        for (int i = 1; i < tournamentSize; i++) {
            PlacementSolution candidate = population.get(random.nextInt(population.size()));
            if (candidate.getFitnessScore() > best.getFitnessScore()) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Crossover: combine two parent solutions
     */
    private PlacementSolution crossover(PlacementSolution parent1, PlacementSolution parent2) {
        PlacementSolution offspring = new PlacementSolution(parent1.getForestWidth(), parent1.getForestHeight());
        Random random = new Random();

        List<PlantPosition> positions1 = parent1.getPlantPositions();
        List<PlantPosition> positions2 = parent2.getPlantPositions();

        int crossoverPoint = random.nextInt(Math.min(positions1.size(), positions2.size()));

        // Take first part from parent1
        for (int i = 0; i < crossoverPoint; i++) {
            PlantPosition pos = positions1.get(i);
            if (!offspring.isPositionOccupied(pos.getX(), pos.getY())) {
                offspring.addPlantPosition(new PlantPosition(pos));
            }
        }

        // Take second part from parent2
        for (int i = crossoverPoint; i < positions2.size(); i++) {
            PlantPosition pos = positions2.get(i);
            if (!offspring.isPositionOccupied(pos.getX(), pos.getY())) {
                offspring.addPlantPosition(new PlantPosition(pos));
            }
        }

        return offspring;
    }

    /**
     * Mutation: randomly modify solution
     */
    private void mutate(PlacementSolution solution) {
        Random random = new Random();
        List<PlantPosition> positions = solution.getPlantPositions();

        if (positions.isEmpty()) return;

        // Randomly swap two plants
        int idx1 = random.nextInt(positions.size());
        int idx2 = random.nextInt(positions.size());

        PlantPosition pos1 = positions.get(idx1);
        PlantPosition pos2 = positions.get(idx2);

        int tempX = pos1.getX();
        int tempY = pos1.getY();

        pos1.setX(pos2.getX());
        pos1.setY(pos2.getY());
        pos2.setX(tempX);
        pos2.setY(tempY);
    }

    /**
     * Generate heatmap showing optimal zones for each species
     */
    public List<HeatmapCell> generateHeatmap(String forestId, String targetSpecies,
                                              List<Plant> existingPlants, int width, int height) {
        List<HeatmapCell> heatmap = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double score = calculatePositionScore(x, y, targetSpecies, existingPlants);
                heatmap.add(new HeatmapCell(x, y, score, targetSpecies));
            }
        }

        return heatmap;
    }

    /**
     * Calculate score for placing a specific species at (x, y)
     */
    private double calculatePositionScore(int x, int y, String targetSpecies, List<Plant> existingPlants) {
        double score = 0.5; // Base score

        for (Plant plant : existingPlants) {
            if (plant.getX() == null || plant.getY() == null) continue;

            double dx = x - plant.getX();
            double dy = y - plant.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance == 0) {
                return 0.0; // Position occupied
            }

            // Companion planting effect
            if (distance <= 3.0) {
                double compatScore = getCompatibilityScore(targetSpecies, plant.getSpecies().getName());
                score += compatScore * (4.0 - distance) * 0.1;
            }

            // Same species spacing
            if (targetSpecies.equals(plant.getSpecies().getName())) {
                if (distance < 3.0) {
                    score -= (3.0 - distance) * 0.3;
                } else if (distance >= 3.0 && distance <= 5.0) {
                    score += 0.1;
                }
            }

            // Resource competition
            if (distance < 2.0) {
                score -= (2.0 - distance) * 0.2;
            }
        }

        // Normalize score to 0-1 range
        return Math.max(0.0, Math.min(1.0, score));
    }
}
