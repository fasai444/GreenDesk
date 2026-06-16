package org.example.services;

import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    public Map<String, Object> getGlobalStats() {
        List<Plant> plants = plantRepository.findAll();
        List<Forest> forests = forestRepository.findAll();

        long total    = plants.size();
        long healthy  = plants.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count();
        long diseased = plants.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count();
        long stressed = plants.stream().filter(p -> p.getPlantState() == PlantState.STRESSED).count();
        long dormant  = plants.stream().filter(p -> p.getPlantState() == PlantState.DORMANT).count();

        double avgStress = plants.stream()
                .mapToDouble(Plant::getStressIndex)
                .average()
                .orElse(0.0);

        double healthRate  = total > 0 ? (double) healthy  / total * 100 : 0;
        double diseaseRate = total > 0 ? (double) diseased / total * 100 : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPlants",    total);
        stats.put("totalForests",   forests.size());
        stats.put("healthyCount",   healthy);
        stats.put("stressedCount",  stressed);
        stats.put("dormantCount",   dormant);
        stats.put("diseasedCount",  diseased);
        stats.put("healthRate",     Math.round(healthRate  * 10.0) / 10.0);
        stats.put("diseaseRate",    Math.round(diseaseRate * 10.0) / 10.0);
        stats.put("avgStressIndex", Math.round(avgStress   * 1000.0) / 1000.0);
        stats.put("totalSpecies",   speciesRepository.findAll().size());
        return stats;
    }

    public Map<String, Long> getStressDistribution() {
        List<Plant> plants = plantRepository.findAll();
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("HEALTHY",  plants.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count());
        dist.put("STRESSED", plants.stream().filter(p -> p.getPlantState() == PlantState.STRESSED).count());
        dist.put("DORMANT",  plants.stream().filter(p -> p.getPlantState() == PlantState.DORMANT).count());
        dist.put("DISEASED", plants.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count());
        return dist;
    }

    public List<Map<String, Object>> getForestHealthRanking() {
        List<Forest> forests = forestRepository.findAll();
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (Forest forest : forests) {
            List<Plant> plants = plantRepository.findByForestId(forest.getId());
            if (plants.isEmpty()) continue;

            long total    = plants.size();
            long healthy  = plants.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count();
            long diseased = plants.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count();
            double avgStress = plants.stream().mapToDouble(Plant::getStressIndex).average().orElse(0);
            double score = (healthy * 100.0 / total) - (diseased * 50.0 / total) - (avgStress * 30);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("forestId",     forest.getId());
            entry.put("forestName",   forest.getName());
            entry.put("totalPlants",  total);
            entry.put("healthyCount", healthy);
            entry.put("diseasedCount",diseased);
            entry.put("healthRate",   Math.round(healthy * 1000.0 / total) / 10.0);
            entry.put("avgStress",    Math.round(avgStress * 1000.0) / 1000.0);
            entry.put("score",        Math.round(score * 10.0) / 10.0);
            ranking.add(entry);
        }

        ranking.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return ranking;
    }

    public List<Map<String, Object>> getSpeciesDistribution() {
        List<Plant> plants = plantRepository.findAll();

        Map<String, Long> countBySpecies = plants.stream()
                .filter(p -> p.getSpecies() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getSpecies().getId(),
                        Collectors.counting()
                ));

        Map<String, String> speciesNames = plants.stream()
                .filter(p -> p.getSpecies() != null)
                .collect(Collectors.toMap(
                        p -> p.getSpecies().getId(),
                        p -> p.getSpecies().getName(),
                        (a, b) -> a
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        countBySpecies.forEach((speciesId, count) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("speciesId",   speciesId);
            entry.put("speciesName", speciesNames.getOrDefault(speciesId, "Inconnu"));
            entry.put("count",       count);
            result.add(entry);
        });

        result.sort((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")));
        return result;
    }

    public List<Map<String, Object>> getTopAtRiskPlants(int limit) {
        List<Plant> plants = plantRepository.findAll();

        return plants.stream()
                .filter(p -> p.getPlantState() != PlantState.HEALTHY)
                .sorted((a, b) -> Double.compare(b.getStressIndex(), a.getStressIndex()))
                .limit(limit)
                .map(p -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("plantId",     p.getId());
                    entry.put("plantName",   p.getName());
                    entry.put("state",       p.getPlantState() != null ? p.getPlantState().name() : "UNKNOWN");
                    entry.put("stressIndex", Math.round(p.getStressIndex() * 1000.0) / 1000.0);
                    entry.put("forestId",    p.getForestId());
                    entry.put("speciesName", p.getSpecies() != null ? p.getSpecies().getName() : "Inconnu");
                    entry.put("temperature", p.getTemperature());
                    entry.put("waterLevel",  p.getWaterLevel());
                    entry.put("humidity",    p.getHumidity());
                    entry.put("luminosity",  p.getLux());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSpeciesHealthStats() {
        List<Plant> plants = plantRepository.findAll();

        Map<String, List<Plant>> bySpecies = plants.stream()
                .filter(p -> p.getSpecies() != null)
                .collect(Collectors.groupingBy(p -> p.getSpecies().getId()));

        Map<String, String> speciesNames = plants.stream()
                .filter(p -> p.getSpecies() != null)
                .collect(Collectors.toMap(
                        p -> p.getSpecies().getId(),
                        p -> p.getSpecies().getName(),
                        (a, b) -> a
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        bySpecies.forEach((speciesId, group) -> {
            long total    = group.size();
            long healthy  = group.stream().filter(p -> p.getPlantState() == PlantState.HEALTHY).count();
            long stressed = group.stream().filter(p -> p.getPlantState() == PlantState.STRESSED).count();
            long dormant  = group.stream().filter(p -> p.getPlantState() == PlantState.DORMANT).count();
            long diseased = group.stream().filter(p -> p.getPlantState() == PlantState.DISEASED).count();
            double avgStress  = group.stream().mapToDouble(Plant::getStressIndex).average().orElse(0);
            double healthRate = total > 0 ? (double) healthy / total * 100 : 0;

            String risk = avgStress >= 0.7  ? "CRITIQUE" :
                          avgStress >= 0.45 ? "ÉLEVÉ"    :
                          avgStress >= 0.2  ? "MODÉRÉ"   : "FAIBLE";

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("speciesId",   speciesId);
            entry.put("speciesName", speciesNames.getOrDefault(speciesId, "Inconnu"));
            entry.put("total",       total);
            entry.put("healthy",     healthy);
            entry.put("stressed",    stressed);
            entry.put("dormant",     dormant);
            entry.put("diseased",    diseased);
            entry.put("healthRate",  Math.round(healthRate * 10.0) / 10.0);
            entry.put("avgStress",   Math.round(avgStress * 1000.0) / 1000.0);
            entry.put("riskLevel",   risk);
            result.add(entry);
        });

        result.sort((a, b) -> Double.compare((double) b.get("avgStress"), (double) a.get("avgStress")));
        return result;
    }

    public List<Map<String, Object>> getEnvironmentalAverages() {
        List<Forest> forests = forestRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Forest forest : forests) {
            List<Plant> plants = plantRepository.findByForestId(forest.getId());
            if (plants.isEmpty()) continue;

            double avgTemp  = plants.stream().mapToDouble(Plant::getTemperature).average().orElse(0);
            double avgWater = plants.stream().mapToDouble(Plant::getWaterLevel).average().orElse(0);
            double avgHum   = plants.stream().mapToDouble(Plant::getHumidity).average().orElse(0);
            double avgLux   = plants.stream().mapToDouble(Plant::getLux).average().orElse(0);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("forestId",    forest.getId());
            entry.put("forestName",  forest.getName());
            entry.put("avgTemp",     Math.round(avgTemp  * 10.0) / 10.0);
            entry.put("avgWater",    Math.round(avgWater * 10.0) / 10.0);
            entry.put("avgHumidity", Math.round(avgHum   * 10.0) / 10.0);
            entry.put("avgLux",      Math.round(avgLux   * 10.0) / 10.0);
            entry.put("plantCount",  plants.size());
            result.add(entry);
        }
        return result;
    }
}
