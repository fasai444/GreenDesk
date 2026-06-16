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

        long total = plants.size();
        long healthy = plants.stream().filter(p -> p.getState() == PlantState.HEALTHY).count();
        long diseased = plants.stream().filter(p -> p.getState() == PlantState.DISEASED).count();
        long stressed = plants.stream().filter(p -> p.getState() == PlantState.STRESSED).count();
        long dormant  = plants.stream().filter(p -> p.getState() == PlantState.DORMANT).count();

        double avgStress = plants.stream()
                .mapToDouble(Plant::calculateStressIndex)
                .average()
                .orElse(0.0);

        double healthRate = total > 0 ? (double) healthy / total * 100 : 0;
        double diseaseRate = total > 0 ? (double) diseased / total * 100 : 0;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPlants", total);
        stats.put("totalForests", forests.size());
        stats.put("healthyCount", healthy);
        stats.put("stressedCount", stressed);
        stats.put("dormantCount", dormant);
        stats.put("diseasedCount", diseased);
        stats.put("healthRate", Math.round(healthRate * 10.0) / 10.0);
        stats.put("diseaseRate", Math.round(diseaseRate * 10.0) / 10.0);
        stats.put("avgStressIndex", Math.round(avgStress * 1000.0) / 1000.0);
        stats.put("totalSpecies", speciesRepository.findAll().size());
        return stats;
    }

    public Map<String, Long> getStressDistribution() {
        List<Plant> plants = plantRepository.findAll();
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("HEALTHY",  plants.stream().filter(p -> p.getState() == PlantState.HEALTHY).count());
        dist.put("STRESSED", plants.stream().filter(p -> p.getState() == PlantState.STRESSED).count());
        dist.put("DORMANT",  plants.stream().filter(p -> p.getState() == PlantState.DORMANT).count());
        dist.put("DISEASED", plants.stream().filter(p -> p.getState() == PlantState.DISEASED).count());
        return dist;
    }

    public List<Map<String, Object>> getForestHealthRanking() {
        List<Forest> forests = forestRepository.findAll();
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (Forest forest : forests) {
            List<Plant> plants = plantRepository.findByForestId(forest.getId());
            if (plants.isEmpty()) continue;

            long total   = plants.size();
            long healthy = plants.stream().filter(p -> p.getState() == PlantState.HEALTHY).count();
            long diseased= plants.stream().filter(p -> p.getState() == PlantState.DISEASED).count();
            double avgStress = plants.stream().mapToDouble(Plant::calculateStressIndex).average().orElse(0);
            double score = (healthy * 100.0 / total) - (diseased * 50.0 / total) - (avgStress * 30);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("forestId",   forest.getId());
            entry.put("forestName", forest.getName());
            entry.put("totalPlants", total);
            entry.put("healthyCount", healthy);
            entry.put("diseasedCount", diseased);
            entry.put("healthRate", Math.round(healthy * 1000.0 / total) / 10.0);
            entry.put("avgStress", Math.round(avgStress * 1000.0) / 1000.0);
            entry.put("score", Math.round(score * 10.0) / 10.0);
            ranking.add(entry);
        }

        ranking.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return ranking;
    }

    public List<Map<String, Object>> getSpeciesDistribution() {
        List<Plant> plants = plantRepository.findAll();
        List<Species> allSpecies = speciesRepository.findAll();

        Map<String, String> speciesNames = allSpecies.stream()
                .collect(Collectors.toMap(Species::getId, Species::getName, (a, b) -> a));

        Map<String, Long> countBySpecies = plants.stream()
                .filter(p -> p.getSpeciesId() != null)
                .collect(Collectors.groupingBy(Plant::getSpeciesId, Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        countBySpecies.forEach((speciesId, count) -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("speciesId", speciesId);
            entry.put("speciesName", speciesNames.getOrDefault(speciesId, "Inconnu"));
            entry.put("count", count);
            result.add(entry);
        });

        result.sort((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")));
        return result;
    }

    public List<Map<String, Object>> getTopAtRiskPlants(int limit) {
        List<Plant> plants = plantRepository.findAll();
        List<Species> allSpecies = speciesRepository.findAll();
        Map<String, String> speciesNames = allSpecies.stream()
                .collect(Collectors.toMap(Species::getId, Species::getName, (a, b) -> a));

        return plants.stream()
                .filter(p -> p.getState() != PlantState.HEALTHY)
                .sorted((a, b) -> Double.compare(b.calculateStressIndex(), a.calculateStressIndex()))
                .limit(limit)
                .map(p -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("plantId",     p.getId());
                    entry.put("plantName",   p.getName());
                    entry.put("state",       p.getState() != null ? p.getState().name() : "UNKNOWN");
                    entry.put("stressIndex", Math.round(p.calculateStressIndex() * 1000.0) / 1000.0);
                    entry.put("forestId",    p.getForestId());
                    entry.put("speciesName", p.getSpeciesId() != null
                            ? speciesNames.getOrDefault(p.getSpeciesId(), "Inconnu") : "Inconnu");
                    entry.put("temperature",  p.getTemperature());
                    entry.put("waterLevel",   p.getWaterLevel());
                    entry.put("humidity",     p.getHumidity());
                    entry.put("luminosity",   p.getLuminosity());
                    return entry;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSpeciesHealthStats() {
        List<Plant> plants = plantRepository.findAll();
        List<Species> allSpecies = speciesRepository.findAll();
        Map<String, String> speciesNames = allSpecies.stream()
                .collect(Collectors.toMap(Species::getId, Species::getName, (a, b) -> a));

        Map<String, List<Plant>> bySpecies = plants.stream()
                .filter(p -> p.getSpeciesId() != null)
                .collect(Collectors.groupingBy(Plant::getSpeciesId));

        List<Map<String, Object>> result = new ArrayList<>();
        bySpecies.forEach((speciesId, group) -> {
            long total    = group.size();
            long healthy  = group.stream().filter(p -> p.getState() == PlantState.HEALTHY).count();
            long stressed = group.stream().filter(p -> p.getState() == PlantState.STRESSED).count();
            long dormant  = group.stream().filter(p -> p.getState() == PlantState.DORMANT).count();
            long diseased = group.stream().filter(p -> p.getState() == PlantState.DISEASED).count();
            double avgStress = group.stream().mapToDouble(Plant::calculateStressIndex).average().orElse(0);
            double healthRate = total > 0 ? (double) healthy / total * 100 : 0;

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
            String risk = avgStress >= 0.7 ? "CRITIQUE" : avgStress >= 0.45 ? "ÉLEVÉ" :
                          avgStress >= 0.2 ? "MODÉRÉ"   : "FAIBLE";
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

            OptionalDouble avgTemp  = plants.stream().mapToDouble(p -> p.getTemperature() != null  ? p.getTemperature()  : 0).average();
            OptionalDouble avgWater = plants.stream().mapToDouble(p -> p.getWaterLevel()  != null  ? p.getWaterLevel()   : 0).average();
            OptionalDouble avgHum   = plants.stream().mapToDouble(p -> p.getHumidity()    != null  ? p.getHumidity()     : 0).average();
            OptionalDouble avgLux   = plants.stream().mapToDouble(p -> p.getLuminosity()  != null  ? p.getLuminosity()   : 0).average();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("forestId",    forest.getId());
            entry.put("forestName",  forest.getName());
            entry.put("avgTemp",     Math.round(avgTemp.orElse(0)  * 10.0) / 10.0);
            entry.put("avgWater",    Math.round(avgWater.orElse(0) * 10.0) / 10.0);
            entry.put("avgHumidity", Math.round(avgHum.orElse(0)   * 10.0) / 10.0);
            entry.put("avgLux",      Math.round(avgLux.orElse(0)   * 10.0) / 10.0);
            entry.put("plantCount",  plants.size());
            result.add(entry);
        }
        return result;
    }
}
