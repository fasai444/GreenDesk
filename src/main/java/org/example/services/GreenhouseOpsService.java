package org.example.services;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.entities.alert.PlantAlert;
import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantEffect;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantAlertRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
import org.example.repositories.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class GreenhouseOpsService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private PlantEffectRepository plantEffectRepository;

    @Autowired
    private PlantAlertRepository plantAlertRepository;

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private SensorReadingService sensorReadingService;

    public Map<String, Object> getOverview() {
        List<Plant> plants = plantRepository.findAll();
        LocalDateTime since24h = LocalDateTime.now().minusHours(24);

        double avgStressPct = plants.isEmpty()
                ? 0.0
                : plants.stream().mapToDouble(Plant::getStressIndex).average().orElse(0.0) * 100.0;

        long healthyPlants = plants.stream()
                .filter(p -> p.getPlantState() != null && "HEALTHY".equalsIgnoreCase(p.getPlantState().name()))
                .count();

        double healthyRate = plants.isEmpty() ? 0.0 : (healthyPlants * 100.0 / plants.size());

        Map<String, Object> summary = new HashMap<>();
        summary.put("species", speciesRepository.count());
        summary.put("plants", plants.size());
        summary.put("forests", forestRepository.count());
        summary.put("activeEffects", plantEffectRepository.countByActiveTrue());
        summary.put("activeAlerts", plantAlertRepository.countByAcknowledgedFalse());
        summary.put("sensorReadings24h",
                sensorReadingRepository.findByTimestampGreaterThanEqualOrderByTimestampDesc(since24h).size());
        summary.put("avgStressPct", Math.round(avgStressPct * 10.0) / 10.0);
        summary.put("healthyRatePct", Math.round(healthyRate * 10.0) / 10.0);

        return summary;
    }

    public List<Map<String, Object>> getLiveEffectsImpact(int limit) {
        List<Plant> plants = plantRepository.findAll().stream()
                .filter(p -> p.getForestId() != null && !p.getForestId().isBlank())
                .toList();

        Map<String, String> forestNameById = forestRepository.findAll().stream()
                .collect(Collectors.toMap(Forest::getId, Forest::getName, (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Plant plant : plants) {
            List<PlantEffect> activeEffects = plantEffectRepository.findByPlantIdAndActive(plant.getId(), true);
            if (activeEffects.isEmpty()) {
                continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("plantId", plant.getId());
            row.put("plantName", plant.getName());
            row.put("forestId", plant.getForestId());
            row.put("forestName", forestNameById.getOrDefault(plant.getForestId(), "N/A"));
            row.put("stressPct", Math.round(plant.getStressIndex() * 1000.0) / 10.0);
            row.put("activeEffects", activeEffects.stream().map(PlantEffect::getEffectId).toList());
            row.put("activeEffectsCount", activeEffects.size());
            row.put("state", plant.getPlantState() == null ? "N/A" : plant.getPlantState().name());
            result.add(row);
        }

        result.sort(Comparator
                .comparing((Map<String, Object> m) -> ((Number) m.get("activeEffectsCount")).intValue()).reversed()
                .thenComparing((Map<String, Object> m) -> ((Number) m.get("stressPct")).doubleValue(),
                        Comparator.reverseOrder()));

        return result.stream().limit(Math.max(1, limit)).toList();
    }

    public List<Map<String, Object>> getActiveAlerts(int hours, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(Math.max(1, hours));

        List<PlantAlert> alerts = plantAlertRepository
                .findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(since);

        Map<String, Plant> plantById = plantRepository.findAll().stream()
                .collect(Collectors.toMap(Plant::getId, p -> p, (a, b) -> a));

        return alerts.stream()
                .limit(Math.max(1, limit))
                .map(alert -> {
                    Plant plant = plantById.get(alert.getPlantId());
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", alert.getId());
                    row.put("createdAt", alert.getCreatedAt());
                    row.put("type", alert.getType() == null ? "UNKNOWN" : alert.getType().name());
                    row.put("severity", alert.getSeverity() == null ? "UNKNOWN" : alert.getSeverity().name());
                    row.put("message", alert.getMessage());
                    row.put("plantId", alert.getPlantId());
                    row.put("plantName", plant == null ? "N/A" : plant.getName());
                    row.put("forestId", plant == null ? "" : (plant.getForestId() == null ? "" : plant.getForestId()));
                    return row;
                })
                .toList();
    }

    public Map<String, Object> getRoiInsights() {
        return getRoiInsights(24);
    }

    public Map<String, Object> getRoiInsights(int hours) {
        List<Plant> plants = plantRepository.findAll();
        int plantsCount = plants.size();
        int windowHours = Math.max(1, hours);
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);

        double avgStress = plantsCount == 0
                ? 0.0
                : plants.stream().mapToDouble(Plant::getStressIndex).average().orElse(0.0);

        long activeAlerts = plantAlertRepository
                .findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(since)
                .size();
        long activeEffects = plantEffectRepository.countByActiveTrue();
        int readings24h = sensorReadingRepository
                .findByTimestampGreaterThanEqualOrderByTimestampDesc(since)
                .size();

        double baselineWaterLiters = plantsCount * 2.6;
        double optimizationFactor = clamp((activeEffects * 0.012) + (readings24h * 0.0008) - (avgStress * 0.22), 0.0,
                0.30);
        double estimatedWaterSavingsLiters = round2(baselineWaterLiters * optimizationFactor);

        double riskIndex = round2(clamp((avgStress * 70.0) + (activeAlerts * 1.8), 0.0, 100.0));

        double baseCostPerPlant = 0.38;
        double alertPenalty = activeAlerts * 0.12;
        double stressPenalty = avgStress * plantsCount * 0.18;
        double effectBenefit = activeEffects * 0.04;
        double estimatedDailyCost = round2(
                (plantsCount * baseCostPerPlant) + alertPenalty + stressPenalty - effectBenefit);

        String recommendation;
        if (riskIndex >= 70) {
            recommendation = "Risque élevé: réduire stress (arrosage ciblé + ombrage), traiter les alertes critiques d'abord.";
        } else if (riskIndex >= 40) {
            recommendation = "Risque modéré: maintenir la fréquence capteurs et renforcer les effets préventifs sur zones sensibles.";
        } else {
            recommendation = "Risque maîtrisé: continuer la stratégie actuelle et optimiser progressivement la consommation d'eau.";
        }

        Map<String, Object> roi = new HashMap<>();
        roi.put("plants", plantsCount);
        roi.put("activeAlerts", activeAlerts);
        roi.put("activeEffects", activeEffects);
        roi.put("sensorReadings24h", readings24h);
        roi.put("avgStress", round2(avgStress));
        roi.put("estimatedWaterSavingsLiters", estimatedWaterSavingsLiters);
        roi.put("riskIndex", riskIndex);
        roi.put("estimatedDailyCostEUR", estimatedDailyCost);
        roi.put("recommendation", recommendation);
        roi.put("model", "greenhouse-roi-v1");
        roi.put("windowHours", windowHours);
        return roi;
    }

    public List<Map<String, Object>> getForestRoiRanking(int limit) {
        return getForestRoiRanking(limit, 24);
    }

    public List<Map<String, Object>> getForestRoiRanking(int limit, int hours) {
        List<Forest> forests = forestRepository.findAll();
        List<Map<String, Object>> ranking = new ArrayList<>();
        int windowHours = Math.max(1, hours);
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);

        for (Forest forest : forests) {
            List<Plant> plants = plantRepository.findByForestId(forest.getId());
            int plantsCount = plants.size();

            double avgStress = plantsCount == 0
                    ? 0.0
                    : plants.stream().mapToDouble(Plant::getStressIndex).average().orElse(0.0);

            List<String> plantIds = plants.stream().map(Plant::getId).toList();
            long activeAlerts = plantIds.isEmpty()
                    ? 0L
                    : plantAlertRepository
                            .findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(since)
                            .stream()
                            .filter(alert -> plantIds.contains(alert.getPlantId()))
                            .count();

            long activeEffects = plantIds.isEmpty()
                    ? 0L
                    : plants.stream()
                            .mapToLong(
                                    plant -> plantEffectRepository.findByPlantIdAndActive(plant.getId(), true).size())
                            .sum();

            double baselineWaterLiters = plantsCount * 2.6;
            double optimizationFactor = clamp((activeEffects * 0.012) - (avgStress * 0.22), 0.0, 0.30);
            double estimatedWaterSavingsLiters = round2(baselineWaterLiters * optimizationFactor);

            double riskIndex = round2(clamp((avgStress * 70.0) + (activeAlerts * 1.8), 0.0, 100.0));

            double baseCostPerPlant = 0.38;
            double alertPenalty = activeAlerts * 0.12;
            double stressPenalty = avgStress * plantsCount * 0.18;
            double effectBenefit = activeEffects * 0.04;
            double estimatedDailyCost = round2(
                    (plantsCount * baseCostPerPlant) + alertPenalty + stressPenalty - effectBenefit);

            double roiScore = round2(
                    clamp((estimatedWaterSavingsLiters * 0.9) - (riskIndex * 0.15) - (estimatedDailyCost * 0.4), -100.0,
                            100.0));

            String level;
            if (roiScore >= 20) {
                level = "RENTABLE";
            } else if (roiScore >= 0) {
                level = "STABLE";
            } else {
                level = "A_RISQUE";
            }

            Map<String, Object> row = new HashMap<>();
            row.put("forestId", forest.getId());
            row.put("forestName", forest.getName());
            row.put("plants", plantsCount);
            row.put("avgStress", round2(avgStress));
            row.put("activeAlerts", activeAlerts);
            row.put("activeEffects", activeEffects);
            row.put("estimatedWaterSavingsLiters", estimatedWaterSavingsLiters);
            row.put("riskIndex", riskIndex);
            row.put("estimatedDailyCostEUR", estimatedDailyCost);
            row.put("roiScore", roiScore);
            row.put("level", level);
            row.put("windowHours", windowHours);
            ranking.add(row);
        }

        ranking.sort(
                Comparator.comparing((Map<String, Object> m) -> ((Number) m.get("roiScore")).doubleValue()).reversed());
        return ranking.stream().limit(Math.max(1, limit)).toList();
    }

    public Map<String, Object> emitSensorTick(String forestId, String profile) throws Exception {
        Forest forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new Exception("Forêt introuvable: " + forestId));

        List<Plant> plants = plantRepository.findByForestId(forestId);
        if (plants.isEmpty()) {
            throw new Exception("Aucune plante dans la forêt: " + forest.getName());
        }

        int created = 0;
        int failed = 0;
        double tempTotal = 0.0;
        double humTotal = 0.0;
        double luxTotal = 0.0;
        double rainTotal = 0.0;

        for (Plant plant : plants) {
            try {
                CreateSensorReadingRequest req = new CreateSensorReadingRequest();
                SensorVector vector = sensorVectorForProfile(plant, profile);
                req.setTimestamp(LocalDateTime.now());
                req.setTemperature(vector.temperature());
                req.setHumidity(vector.humidity());
                req.setLux(vector.lux());
                req.setRainfall(vector.rainfall());

                SensorReading reading = sensorReadingService.addReading(plant.getId(), req);
                if (reading != null) {
                    created += 1;
                    tempTotal += vector.temperature();
                    humTotal += vector.humidity();
                    luxTotal += vector.lux();
                    rainTotal += vector.rainfall();
                }
            } catch (Exception e) {
                failed += 1;
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("forestId", forest.getId());
        out.put("forestName", forest.getName());
        out.put("profile", profile == null ? "NORMAL" : profile.toUpperCase());
        out.put("targetedPlants", plants.size());
        out.put("createdReadings", created);
        out.put("failedReadings", failed);
        out.put("avgTemperature", created == 0 ? 0.0 : round2(tempTotal / created));
        out.put("avgHumidity", created == 0 ? 0.0 : round2(humTotal / created));
        out.put("avgLux", created == 0 ? 0.0 : round2(luxTotal / created));
        out.put("avgRainfall", created == 0 ? 0.0 : round2(rainTotal / created));
        out.put("timestamp", LocalDateTime.now());
        return out;
    }

    private SensorVector sensorVectorForProfile(Plant plant, String profile) {
        String mode = profile == null ? "NORMAL" : profile.trim().toUpperCase();

        double baseTemp = plant.getTemperature();
        double baseHum = plant.getHumidity();
        double baseLux = plant.getLux();

        if ("HOT_DRY".equals(mode)) {
            return new SensorVector(
                    round2(noise(baseTemp + 6.0, 1.8)),
                    round2(clamp(noise(baseHum - 15.0, 5.5), 5.0, 100.0)),
                    round2(clamp(noise(baseLux + 1800.0, 700.0), 0.0, 160000.0)),
                    round2(clamp(noise(0.6, 0.5), 0.0, 12.0)));
        }

        if ("HUMID_RAIN".equals(mode)) {
            return new SensorVector(
                    round2(noise(baseTemp - 2.0, 1.2)),
                    round2(clamp(noise(baseHum + 18.0, 6.0), 5.0, 100.0)),
                    round2(clamp(noise(baseLux - 1400.0, 650.0), 0.0, 160000.0)),
                    round2(clamp(noise(4.8, 1.0), 0.0, 12.0)));
        }

        return new SensorVector(
                round2(noise(baseTemp, 1.0)),
                round2(clamp(noise(baseHum, 3.5), 5.0, 100.0)),
                round2(clamp(noise(baseLux, 500.0), 0.0, 160000.0)),
                round2(clamp(noise(1.0, 0.4), 0.0, 12.0)));
    }

    private double noise(double base, double amplitude) {
        return base + ThreadLocalRandom.current().nextDouble(-amplitude, amplitude);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record SensorVector(double temperature, double humidity, double lux, double rainfall) {
    }
}
