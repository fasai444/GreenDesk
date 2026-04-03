package org.example.controllers.weather;

import org.example.dto.weather.TomorrowWebhookPayload;
import org.example.entities.weather.PlantImpact;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.PlantImpactRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.services.weather.WebhookReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weather")
public class WeatherWebhookController {

    @Autowired
    private WebhookReceiverService webhookReceiverService;

    @Autowired
    private WeatherAlertRepository weatherAlertRepository;

    @Autowired
    private PlantImpactRepository plantImpactRepository;

    // ⬇️⬇️⬇️ AJOUTER CES SETTERS POUR LES TESTS ⬇️⬇️⬇️
    public void setWebhookReceiverService(WebhookReceiverService webhookReceiverService) {
        this.webhookReceiverService = webhookReceiverService;
    }

    public void setWeatherAlertRepository(WeatherAlertRepository weatherAlertRepository) {
        this.weatherAlertRepository = weatherAlertRepository;
    }

    public void setPlantImpactRepository(PlantImpactRepository plantImpactRepository) {
        this.plantImpactRepository = plantImpactRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> receiveWebhook(@RequestBody TomorrowWebhookPayload payload) {
        try {
            webhookReceiverService.processWebhook(payload);
            return ResponseEntity.ok().body(Map.of("status", "processed"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(@RequestParam(required = false) String plantId,
                                    @RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            List<WeatherAlert> alerts = new ArrayList<>();
            
            if (plantId != null && !plantId.isEmpty()) {
                List<PlantImpact> impacts = plantImpactRepository.findByPlantIdOrderByTimestampDesc(plantId);
                if (impacts != null && !impacts.isEmpty()) {
                    List<String> alertIds = impacts.stream()
                            .map(PlantImpact::getAlertId)
                            .distinct()
                            .toList();
                    if (!alertIds.isEmpty()) {
                        alerts = weatherAlertRepository.findAllById(alertIds);
                    }
                }
            } else {
                List<WeatherAlert> allAlerts = weatherAlertRepository.findAll();
                if (allAlerts != null) {
                    alerts = allAlerts;
                }
            }
            
            if (alerts == null) {
                alerts = new ArrayList<>();
            }
            
            if (activeOnly) {
                alerts = alerts.stream()
                        .filter(a -> a != null && !a.isProcessed())
                        .collect(Collectors.toList());
            }
            
            if (alerts != null && !alerts.isEmpty()) {
                alerts.sort((a, b) -> {
                    if (a == null || b == null || a.getTimestamp() == null || b.getTimestamp() == null) {
                        return 0;
                    }
                    return b.getTimestamp().compareTo(a.getTimestamp());
                });
            }
            
            return ResponseEntity.ok(alerts != null ? alerts : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/alerts/{alertId}/ack")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable String alertId) {
        try {
            Optional<WeatherAlert> alertOpt = weatherAlertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            WeatherAlert alert = alertOpt.get();
            alert.setProcessed(true);
            alert.setProcessedAt(LocalDateTime.now());
            weatherAlertRepository.save(alert);
            return ResponseEntity.ok(Map.of("message", "Alerte acquittée"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}