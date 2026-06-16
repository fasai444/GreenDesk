package org.example.services;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WhatIfService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestService forestService;

    // ---------------------------------------------------------------
    // Scénarios prédéfinis
    // ---------------------------------------------------------------
    public List<Map<String, Object>> getPredefinedScenarios() {
        return List.of(
            scenario("CANICULE",       "Canicule",          "fa-temperature-high", "#dc2626",
                     12.0,  0,    -20, 3000,  1),
            scenario("GEL",            "Gel intense",        "fa-snowflake",        "#3b82f6",
                     -15.0, -30,  -10,    0,  1),
            scenario("SECHERESSE",     "Sécheresse",        "fa-tint-slash",       "#d97706",
                     4.0,  -60,  -30, 1000,  3),
            scenario("INONDATION",     "Inondation",        "fa-water",            "#0891b2",
                     -2.0,  100,   40, -500,  1),
            scenario("OMBRE_TOTALE",   "Ombre totale",      "fa-cloud",            "#6b7280",
                     -3.0,   0,   10, -4000,  2),
            scenario("TEMPETE",        "Tempête",           "fa-wind",             "#7c3aed",
                     -6.0,   20,  20, -2000,  1),
            scenario("FERTILISATION",  "Fertilisation intensive", "fa-seedling",   "#16a34a",
                     0.0,    40,  15,  500,  5),
            scenario("CUSTOM",         "Scénario personnalisé", "fa-sliders-h",    "#0f172a",
                     0.0,    0,    0,    0,  1)
        );
    }

    private Map<String, Object> scenario(String id, String label, String icon, String color,
                                          double tempDelta, double waterDelta,
                                          double humidityDelta, double luxDelta, int ticks) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("id", id);
        s.put("label", label);
        s.put("icon", icon);
        s.put("color", color);
        s.put("tempDelta", tempDelta);
        s.put("waterDelta", waterDelta);
        s.put("humidityDelta", humidityDelta);
        s.put("luxDelta", luxDelta);
        s.put("defaultTicks", ticks);
        return s;
    }

    // ---------------------------------------------------------------
    // Simulation principale
    // ---------------------------------------------------------------
    public Map<String, Object> simulate(String forestId,
                                         double tempDelta, double waterDelta,
                                         double humidityDelta, double luxDelta,
                                         int ticks) throws Exception {

        List<Plant> plants = forestService.getPlantsInForest(forestId);
        if (plants.isEmpty()) throw new Exception("Aucune plante dans cette forêt.");

        List<Map<String, Object>> plantResults = new ArrayList<>();
        int degraded = 0, improved = 0, unchanged = 0, critical = 0;
        double totalRiskScore = 0;

        for (Plant plant : plants) {
            if (plant.getSpecies() == null) continue;

            // État AVANT
            double stressBefore = plant.getStressIndex();
            PlantState stateBefore = plant.getPlantState() != null
                    ? plant.getPlantState() : computeState(stressBefore);

            // Simulation N ticks en mémoire (sans toucher à la BDD)
            double simWater    = plant.getWaterLevel();
            double simTemp     = plant.getTemperature();
            double simHumidity = plant.getHumidity();
            double simLux      = plant.getLux();
            double simStress   = stressBefore;

            for (int t = 0; t < ticks; t++) {
                simWater    = Math.max(0, simWater    + waterDelta);
                simTemp     = simTemp     + tempDelta;
                simHumidity = Math.min(100, Math.max(0, simHumidity + humidityDelta));
                simLux      = Math.max(0, simLux      + luxDelta);

                double tickStress = computeStress(plant.getSpecies(), simWater, simTemp, simHumidity, simLux);
                // accumulation progressive (même logique que evolvePlant)
                simStress = Math.min(1.0, Math.max(0.0, simStress + tickStress * 0.2));
            }

            PlantState stateAfter = computeState(simStress);
            double delta = simStress - stressBefore;
            String transition = stateBefore.name() + " → " + stateAfter.name();
            boolean stateChanged = stateBefore != stateAfter;

            String impact;
            if (delta > 0.15)       impact = "CRITICAL";
            else if (delta > 0.08)  impact = "HIGH";
            else if (delta > 0.02)  impact = "MEDIUM";
            else if (delta < -0.02) impact = "IMPROVED";
            else                    impact = "STABLE";

            if ("IMPROVED".equals(impact)) improved++;
            else if ("STABLE".equals(impact)) unchanged++;
            else { degraded++; if ("CRITICAL".equals(impact)) critical++; }

            totalRiskScore += Math.max(0, delta);

            Map<String, Object> pr = new LinkedHashMap<>();
            pr.put("plantId",      plant.getId());
            pr.put("plantName",    plant.getName());
            pr.put("speciesName",  plant.getSpecies().getName());
            pr.put("stressBefore", round3(stressBefore));
            pr.put("stressAfter",  round3(simStress));
            pr.put("stressDelta",  round3(delta));
            pr.put("stateBefore",  stateBefore.name());
            pr.put("stateAfter",   stateAfter.name());
            pr.put("stateChanged", stateChanged);
            pr.put("transition",   transition);
            pr.put("impact",       impact);
            pr.put("envAfter", Map.of(
                "temperature", round1(simTemp),
                "waterLevel",  round1(simWater),
                "humidity",    round1(simHumidity),
                "lux",         round1(simLux)
            ));
            plantResults.add(pr);
        }

        // Trier : les plus impactées en premier
        plantResults.sort(Comparator.comparingDouble(r -> -((double) r.get("stressDelta"))));

        int total = plantResults.size();
        double avgRisk = total > 0 ? totalRiskScore / total : 0;
        String globalRisk;
        if (avgRisk > 0.12 || critical > total * 0.3)       globalRisk = "CRITIQUE";
        else if (avgRisk > 0.06 || degraded > total * 0.5)  globalRisk = "ÉLEVÉ";
        else if (avgRisk > 0.02 || degraded > total * 0.2)  globalRisk = "MODÉRÉ";
        else                                                  globalRisk = "FAIBLE";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("forestId", forestId);
        result.put("ticks", ticks);
        result.put("scenario", Map.of(
            "tempDelta", tempDelta, "waterDelta", waterDelta,
            "humidityDelta", humidityDelta, "luxDelta", luxDelta
        ));
        result.put("summary", Map.of(
            "totalPlants",  total,
            "degraded",     degraded,
            "improved",     improved,
            "unchanged",    unchanged,
            "critical",     critical,
            "globalRisk",   globalRisk,
            "avgRiskScore", round3(avgRisk)
        ));
        result.put("plants", plantResults);
        return result;
    }

    // ---------------------------------------------------------------
    // Calcul de stress (reproduction fidèle de Plant.calculateStressIndex)
    // ---------------------------------------------------------------
    private double computeStress(Species s, double water, double temp, double humidity, double lux) {
        double stress = 0;
        stress += Math.min(1.0, Math.abs(water - s.getOptimalWaterNeeds()) / s.getOptimalWaterNeeds());
        stress += Math.min(1.0, Math.abs(temp  - s.getOptimalTemperature()) / 10.0);
        stress += Math.min(1.0, Math.abs(humidity - s.getOptimalHumidity()) / 20.0);
        stress += Math.min(1.0, s.lightStressFactor(lux));
        return Math.min(1.0, stress);
    }

    private PlantState computeState(double stress) {
        if (stress < 0.3) return PlantState.HEALTHY;
        if (stress < 0.6) return PlantState.STRESSED;
        if (stress < 0.9) return PlantState.DORMANT;
        return PlantState.DISEASED;
    }

    private double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }
    private double round1(double v) { return Math.round(v * 10.0)   / 10.0;   }
}
