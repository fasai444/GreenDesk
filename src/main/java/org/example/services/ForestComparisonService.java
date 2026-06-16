package org.example.services;

import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.repositories.ForestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForestComparisonService {

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private ForestService forestService;

    public Map<String, Object> compareForests(String forestId1, String forestId2) throws Exception {
        Forest forest1 = forestRepository.findById(forestId1)
                .orElseThrow(() -> new Exception("Forêt introuvable : " + forestId1));
        Forest forest2 = forestRepository.findById(forestId2)
                .orElseThrow(() -> new Exception("Forêt introuvable : " + forestId2));

        List<Plant> plants1 = forestService.getPlantsInForest(forestId1);
        List<Plant> plants2 = forestService.getPlantsInForest(forestId2);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forest1", buildForestStats(forest1, plants1));
        result.put("forest2", buildForestStats(forest2, plants2));
        result.put("winner", determineWinner(plants1, plants2, forest1.getName(), forest2.getName()));
        return result;
    }

    private Map<String, Object> buildForestStats(Forest forest, List<Plant> plants) {
        int total = plants.size();

        long healthy = plants.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count();
        long stressed = plants.stream().filter(p -> p.getPlantState() == PlantState.STRESSED).count();
        long dormant = plants.stream().filter(p -> p.getPlantState() == PlantState.DORMANT).count();
        long diseased = plants.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count();

        double avgStress = plants.stream()
                .mapToDouble(Plant::getStressIndex)
                .average()
                .orElse(0.0);

        double healthRate = total > 0 ? (double) healthy / total * 100 : 0;
        double diseaseRate = total > 0 ? (double) diseased / total * 100 : 0;

        long speciesCount = plants.stream()
                .filter(p -> p.getSpecies() != null)
                .map(p -> p.getSpecies().getId())
                .distinct()
                .count();

        int gridSize = forest.getWidth() * forest.getHeight();
        double occupancyRate = gridSize > 0 ? (double) total / gridSize * 100 : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("id", forest.getId());
        stats.put("name", forest.getName());
        stats.put("width", forest.getWidth());
        stats.put("height", forest.getHeight());
        stats.put("gridSize", gridSize);
        stats.put("totalPlants", total);
        stats.put("occupancyRate", Math.round(occupancyRate * 10.0) / 10.0);
        stats.put("healthyCount", healthy);
        stats.put("stressedCount", stressed);
        stats.put("dormantCount", dormant);
        stats.put("diseasedCount", diseased);
        stats.put("healthRate", Math.round(healthRate * 10.0) / 10.0);
        stats.put("diseaseRate", Math.round(diseaseRate * 10.0) / 10.0);
        stats.put("avgStressIndex", Math.round(avgStress * 1000.0) / 1000.0);
        stats.put("speciesCount", speciesCount);
        stats.put("stateDistribution", Map.of(
                "HEALTHY", healthy,
                "STRESSED", stressed,
                "DORMANT", dormant,
                "DISEASED", diseased
        ));
        return stats;
    }

    private Map<String, Object> determineWinner(List<Plant> plants1, List<Plant> plants2,
                                                 String name1, String name2) {
        double score1 = computeScore(plants1);
        double score2 = computeScore(plants2);

        String winner = score1 > score2 ? name1 : (score2 > score1 ? name2 : "Égalité");
        String reason;
        if (score1 > score2) {
            reason = name1 + " a un meilleur score de santé globale (" + Math.round(score1 * 10) / 10.0 + " vs " + Math.round(score2 * 10) / 10.0 + ")";
        } else if (score2 > score1) {
            reason = name2 + " a un meilleur score de santé globale (" + Math.round(score2 * 10) / 10.0 + " vs " + Math.round(score1 * 10) / 10.0 + ")";
        } else {
            reason = "Les deux forêts ont des scores équivalents";
        }

        return Map.of("name", winner, "reason", reason,
                "score1", Math.round(score1 * 10) / 10.0,
                "score2", Math.round(score2 * 10) / 10.0);
    }

    // Score sur 100 : % sains - % malades - stress moyen*50
    private double computeScore(List<Plant> plants) {
        if (plants.isEmpty()) return 0;
        int total = plants.size();
        long healthy = plants.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count();
        long diseased = plants.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count();
        double avgStress = plants.stream().mapToDouble(Plant::getStressIndex).average().orElse(0);
        return ((double) healthy / total * 100) - ((double) diseased / total * 50) - (avgStress * 30);
    }
}
