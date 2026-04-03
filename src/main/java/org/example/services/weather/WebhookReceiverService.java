package org.example.services.weather;

import org.example.dto.weather.TomorrowWebhookPayload;
import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.weather.WeatherAlert;
import org.example.entities.weather.PlantImpact;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.repositories.PlantImpactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;  // ⬅️ AJOUTER CET IMPORT

@Service
public class WebhookReceiverService {
    
    private static final double ALERT_RADIUS_KM = 10.0;
    
    @Autowired
    private WeatherAlertRepository weatherAlertRepository;
    
    @Autowired
    private PlantImpactRepository plantImpactRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private ForestRepository forestRepository;
    
    @Autowired
    private PlantImpactCalculator impactCalculator;
    
    @Autowired
    private PlantStateUpdater stateUpdater;
    
    @Transactional
    public void processWebhook(TomorrowWebhookPayload payload) {
        // 1. Vérifier doublon
        if (weatherAlertRepository.findByEventId(payload.getEvent_id()).isPresent()) {
            return;
        }
        
        // 2. Sauvegarder l'alerte
        WeatherAlert alert = saveAlert(payload);
        
        // 3. Trouver les plantes impactées
        List<Plant> impactedPlants = findImpactedPlants(payload.getCoords());
        
        // 4. Traiter chaque plante
        List<PlantImpact> impacts = new ArrayList<>();
        for (Plant plant : impactedPlants) {
            if (!isPlantSensitive(plant, alert.getType())) {
                continue;
            }
            
            List<PlantImpact> history = plantImpactRepository.findByPlantIdOrderByTimestampDesc(plant.getId());
            
            double isr = impactCalculator.calculateISR(plant, alert);
            double sps = impactCalculator.calculateSPS(plant, isr, history);
            double previousStress = plant.getStressIndex();
            String previousState = plant.getPlantState().name();
            
            stateUpdater.updatePlantState(plant, isr, sps);
            
            PlantImpact impact = new PlantImpact(
                alert.getId(), plant.getId(), isr, sps,
                previousStress, plant.getStressIndex(),
                previousState, plant.getPlantState().name(),
                new ArrayList<>()
            );
            impacts.add(plantImpactRepository.save(impact));
        }
        
        // 5. Marquer l'alerte comme traitée
        alert.setProcessed(true);
        alert.setProcessedAt(LocalDateTime.now());
        weatherAlertRepository.save(alert);
    }
    
    private WeatherAlert saveAlert(TomorrowWebhookPayload payload) {
        LocalDateTime timestamp = LocalDateTime.parse(payload.getTimestamp(), 
                DateTimeFormatter.ISO_DATE_TIME);
        
        WeatherAlert alert = new WeatherAlert(
            payload.getEvent_id(),
            payload.getType(),
            payload.getCoords(),
            timestamp,
            payload.getSeverity(),
            payload.getDetails()
        );
        
        return weatherAlertRepository.save(alert);
    }
    
    private List<Plant> findImpactedPlants(double[] coords) {
        List<Forest> forests = forestRepository.findAll();
        List<String> forestIds = new ArrayList<>();
        
        for (Forest forest : forests) {
            if (forest.getCoords() != null && isWithinRadius(forest.getCoords(), coords, ALERT_RADIUS_KM)) {
                forestIds.add(forest.getId());
            }
        }
        
        if (forestIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // ⬇️⬇️⬇️ APPROCHE ALTERNATIVE (si findByForestIdIn ne fonctionne pas) ⬇️⬇️⬇️
        List<Plant> allPlants = plantRepository.findAll();
        return allPlants.stream()
                .filter(plant -> plant.getForestId() != null && forestIds.contains(plant.getForestId()))
                .collect(Collectors.toList());
    }
    
    @SuppressWarnings("unused")
    private boolean isWithinRadius(double[] point1, double[] point2, double radiusKm) {
        double lat1 = point1[0];
        double lon1 = point1[1];
        double lat2 = point2[0];
        double lon2 = point2[1];
        
        double R = 6371; // Rayon terrestre en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return distance <= radiusKm;
    }
    
    private boolean isPlantSensitive(Plant plant, String alertType) {
        if (plant.getSpecies() == null) return true;
        
        return switch (alertType) {
            case "frost" -> plant.getSpecies().isSensitiveToFrost();
            case "heatwave" -> plant.getSpecies().isSensitiveToHeat();
            case "heavy_rain" -> plant.getSpecies().isSensitiveToHeavyRain();
            case "high_wind" -> plant.getSpecies().isSensitiveToWind();
            case "uv_alert" -> plant.getSpecies().isSensitiveToUV();
            default -> true;
        };
    }
}