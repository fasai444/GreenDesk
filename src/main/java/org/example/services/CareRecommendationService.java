package org.example.services;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CareRecommendationService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private ForestService forestService;

    public List<Map<String, Object>> getRecommendationsForPlant(String plantId) throws Exception {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new Exception("Plante introuvable : " + plantId));
        return buildRecommendations(plant);
    }

    public List<Map<String, Object>> getRecommendationsForForest(String forestId) {
        List<Plant> plants = forestService.getPlantsInForest(forestId);
        List<Map<String, Object>> all = new ArrayList<>();
        for (Plant plant : plants) {
            all.addAll(buildRecommendations(plant));
        }
        all.sort(Comparator.comparingInt(r -> -priorityScore((String) r.get("priority"))));
        return all;
    }

    public List<Map<String, Object>> getRecommendationsForAll() {
        List<Plant> plants = plantRepository.findAllByOrderByStressIndexDesc();
        List<Map<String, Object>> all = new ArrayList<>();
        for (Plant plant : plants) {
            all.addAll(buildRecommendations(plant));
        }
        all.sort(Comparator.comparingInt(r -> -priorityScore((String) r.get("priority"))));
        return all;
    }

    private List<Map<String, Object>> buildRecommendations(Plant plant) {
        List<Map<String, Object>> recs = new ArrayList<>();
        Species species = plant.getSpecies();

        if (species == null) return recs;

        // --- Eau ---
        double waterDiff = plant.getWaterLevel() - species.getOptimalWaterNeeds();
        if (waterDiff < -15) {
            String priority = waterDiff < -50 ? "CRITICAL" : (waterDiff < -30 ? "HIGH" : "MEDIUM");
            recs.add(rec(plant, "WATERING",
                    "Arroser maintenant",
                    String.format("Niveau d'eau %.0f mL — déficit de %.0f mL par rapport à l'optimal (%.0f mL)",
                            plant.getWaterLevel(), Math.abs(waterDiff), species.getOptimalWaterNeeds()),
                    priority,
                    "fa-tint"));
        } else if (waterDiff > 30) {
            recs.add(rec(plant, "DRAINAGE",
                    "Réduire l'arrosage",
                    String.format("Excès d'eau de %.0f mL — risque d'asphyxie racinaire", waterDiff),
                    "MEDIUM", "fa-water"));
        }

        // --- Température ---
        double tempDiff = plant.getTemperature() - species.getOptimalTemperature();
        if (tempDiff < -4) {
            String priority = tempDiff < -10 ? "CRITICAL" : "HIGH";
            recs.add(rec(plant, "HEATING",
                    "Augmenter le chauffage",
                    String.format("Température actuelle %.1f°C — trop froide de %.1f°C (optimal %.1f°C)",
                            plant.getTemperature(), Math.abs(tempDiff), species.getOptimalTemperature()),
                    priority, "fa-thermometer-empty"));
        } else if (tempDiff > 4) {
            String priority = tempDiff > 10 ? "CRITICAL" : "HIGH";
            recs.add(rec(plant, "COOLING",
                    "Déplacer à l'ombre / ventiler",
                    String.format("Température actuelle %.1f°C — trop chaude de %.1f°C (optimal %.1f°C)",
                            plant.getTemperature(), tempDiff, species.getOptimalTemperature()),
                    priority, "fa-thermometer-full"));
        }

        // --- Humidité ---
        double humDiff = plant.getHumidity() - species.getOptimalHumidity();
        if (humDiff < -10) {
            recs.add(rec(plant, "HUMIDITY",
                    "Augmenter l'humidité ambiante",
                    String.format("Humidité %.0f%% — trop sèche de %.0f%% (optimal %.0f%%)",
                            plant.getHumidity(), Math.abs(humDiff), species.getOptimalHumidity()),
                    "MEDIUM", "fa-cloud"));
        } else if (humDiff > 20) {
            recs.add(rec(plant, "VENTILATION",
                    "Aérer / réduire l'humidité",
                    String.format("Humidité %.0f%% — excès de %.0f%% (risque de moisissures)",
                            plant.getHumidity(), humDiff),
                    "MEDIUM", "fa-wind"));
        }

        // --- Luminosité ---
        double luxDiff = plant.getLux() - species.getOptimalLuxNeeds();
        if (luxDiff < -500) {
            String priority = luxDiff < -2000 ? "HIGH" : "MEDIUM";
            recs.add(rec(plant, "LIGHTING",
                    "Exposer à plus de lumière",
                    String.format("Luminosité actuelle %.0f lx — déficit de %.0f lx (optimal %.0f lx)",
                            plant.getLux(), Math.abs(luxDiff), species.getOptimalLuxNeeds()),
                    priority, "fa-sun"));
        } else if (luxDiff > 2000) {
            recs.add(rec(plant, "SHADING",
                    "Mettre à l'ombre",
                    String.format("Luminosité trop élevée %.0f lx — excès de %.0f lx",
                            plant.getLux(), luxDiff),
                    "LOW", "fa-cloud-sun"));
        }

        // --- Etat général critique ---
        if (plant.getPlantState() == PlantState.DISEASED) {
            recs.add(rec(plant, "TREATMENT",
                    "Traitement urgent — plante malade",
                    String.format("Indice de stress critique : %.2f. Isoler et traiter immédiatement.",
                            plant.getStressIndex()),
                    "CRITICAL", "fa-first-aid"));
        } else if (plant.getPlantState() == PlantState.DORMANT && plant.getStressIndex() > 0.7) {
            recs.add(rec(plant, "INTERVENTION",
                    "Intervention requise — dormance anormale",
                    String.format("Stress %.2f — vérifier l'ensemble des conditions environnementales.",
                            plant.getStressIndex()),
                    "HIGH", "fa-exclamation-triangle"));
        }

        return recs;
    }

    private Map<String, Object> rec(Plant plant, String type, String action, String reason,
                                     String priority, String icon) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("plantId", plant.getId());
        r.put("plantName", plant.getName());
        r.put("forestId", plant.getForestId());
        r.put("speciesName", plant.getSpecies() != null ? plant.getSpecies().getName() : null);
        r.put("type", type);
        r.put("action", action);
        r.put("reason", reason);
        r.put("priority", priority);
        r.put("icon", icon);
        r.put("stressIndex", plant.getStressIndex());
        r.put("plantState", plant.getPlantState() != null ? plant.getPlantState().name() : null);
        return r;
    }

    private int priorityScore(String p) {
        return switch (p) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
