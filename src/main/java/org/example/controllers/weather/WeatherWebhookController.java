package org.example.controllers.weather;

import org.example.dto.weather.TomorrowWebhookPayload;
import org.example.entities.plant.Plant;
import org.example.entities.weather.PlantImpact;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.PlantImpactRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.services.NotificationService;
import org.example.services.weather.WeatherAlertConfigService;
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

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private WeatherAlertConfigService weatherAlertConfigService;

    @Autowired
    private NotificationService notificationService;

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
    
    // GET /api/weather/alerts?forestId={id}&activeOnly=true
    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(
            @RequestParam(required = false) String forestId,
            @RequestParam(required = false) String plantId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        try {
            List<WeatherAlert> alerts;

            if (forestId != null && !forestId.isEmpty()) {
                // Filtrer par forêt : plants → impacts → alertIds
                List<Plant> plants = plantRepository.findByForestId(forestId);
                List<String> plantIds = plants.stream().map(Plant::getId).toList();
                List<String> alertIds = plantImpactRepository.findAll().stream()
                        .filter(i -> plantIds.contains(i.getPlantId()))
                        .map(PlantImpact::getAlertId)
                        .distinct()
                        .toList();
                alerts = alertIds.isEmpty()
                        ? new ArrayList<>()
                        : weatherAlertRepository.findAllById(alertIds);

            } else if (plantId != null && !plantId.isEmpty()) {
                // Filtrer par plante
                List<String> alertIds = plantImpactRepository
                        .findByPlantIdOrderByTimestampDesc(plantId).stream()
                        .map(PlantImpact::getAlertId)
                        .distinct()
                        .toList();
                alerts = alertIds.isEmpty()
                        ? new ArrayList<>()
                        : weatherAlertRepository.findAllById(alertIds);

            } else {
                alerts = weatherAlertRepository.findAll();
            }

            if (activeOnly) {
                alerts = alerts.stream()
                        .filter(a -> !a.isAcknowledged())
                        .collect(Collectors.toList());
            }

            alerts.sort((a, b) -> {
                if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                return b.getTimestamp().compareTo(a.getTimestamp());
            });

            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/weather/alerts/{id}/ack
    @PostMapping("/alerts/{alertId}/ack")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable String alertId) {
        try {
            Optional<WeatherAlert> alertOpt = weatherAlertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            WeatherAlert alert = alertOpt.get();
            alert.setAcknowledged(true);
            alert.setAcknowledgedAt(LocalDateTime.now());
            weatherAlertRepository.save(alert);
            return ResponseEntity.ok(Map.of("message", "Alerte acquittée"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/weather/impact/{plantId}
    @GetMapping("/impact/{plantId}")
    public ResponseEntity<?> getPlantImpact(@PathVariable String plantId) {
        try {
            List<PlantImpact> impacts = plantImpactRepository.findByPlantIdOrderByTimestampDesc(plantId);
            return ResponseEntity.ok(impacts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/weather/alert-config
    @PostMapping("/alert-config")
    public ResponseEntity<?> configureAlerts(@RequestBody Map<String, Object> body) {
        try {
            String forestId = (String) body.get("forestId");
            if (forestId == null || forestId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "forestId requis"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> thresholds = (Map<String, Object>) body.get("thresholds");
            Map<String, Object> result = weatherAlertConfigService.configurerAlertesPourForet(forestId, thresholds);
            boolean success = Boolean.TRUE.equals(result.get("success"));
            return success
                    ? ResponseEntity.ok(result)
                    : ResponseEntity.status(500).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/notifications - Récupérer les notifications
    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        try {
            List<NotificationService.Notification> notifications;
            if (unreadOnly) {
                notifications = notificationService.getUnreadNotifications();
            } else {
                notifications = notificationService.getAllNotifications();
            }
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/notifications/{id}/read - Marquer une notification comme lue
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable String id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok(Map.of("message", "Notification marquée comme lue"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/notifications/read-all - Marquer toutes les notifications comme lues
    @PostMapping("/notifications/read-all")
    public ResponseEntity<?> markAllAsRead() {
        try {
            notificationService.markAllAsRead();
            return ResponseEntity.ok(Map.of("message", "Toutes les notifications marquées comme lues"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}