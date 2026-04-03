package org.example.controllers.weather;

import org.example.dto.weather.TomorrowWebhookPayload;
import org.example.entities.weather.PlantImpact;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.PlantImpactRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.services.weather.WebhookReceiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WeatherWebhookControllerTest {

    @Mock
    private WebhookReceiverService webhookReceiverService;

    @Mock
    private WeatherAlertRepository weatherAlertRepository;

    @Mock
    private PlantImpactRepository plantImpactRepository;

    private WeatherWebhookController weatherWebhookController;

    private TomorrowWebhookPayload validPayload;
    private TomorrowWebhookPayload invalidPayload;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Créer le contrôleur et injecter les mocks manuellement via setters
        weatherWebhookController = new WeatherWebhookController();
        weatherWebhookController.setWebhookReceiverService(webhookReceiverService);
        weatherWebhookController.setWeatherAlertRepository(weatherAlertRepository);
        weatherWebhookController.setPlantImpactRepository(plantImpactRepository);
        
        // Créer un payload valide
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 38.0);
        
        validPayload = new TomorrowWebhookPayload();
        validPayload.setEvent_id("test_event_123");
        validPayload.setType("heatwave");
        validPayload.setCoords(new double[]{48.8566, 2.3522});
        validPayload.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        validPayload.setSeverity("high");
        validPayload.setDetails(details);
        
        invalidPayload = new TomorrowWebhookPayload();
        invalidPayload.setType("heatwave");
        
        // Configurer les mocks
        when(weatherAlertRepository.findAll()).thenReturn(new ArrayList<>());
        when(plantImpactRepository.findByPlantIdOrderByTimestampDesc(anyString())).thenReturn(new ArrayList<>());
    }

    // ==================== TESTS POST /webhook ====================

    @Test
    @DisplayName("POST /webhook - Payload valide doit retourner 200 OK")
    void testReceiveWebhook_ValidPayload_ReturnsOk() {
        doNothing().when(webhookReceiverService).processWebhook(any(TomorrowWebhookPayload.class));
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(validPayload);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webhookReceiverService, times(1)).processWebhook(validPayload);
    }

    @Test
    @DisplayName("POST /webhook - Service lève une exception doit retourner 500")
    void testReceiveWebhook_ServiceThrowsException_ReturnsInternalServerError() {
        doThrow(new RuntimeException("Erreur de traitement"))
            .when(webhookReceiverService).processWebhook(any(TomorrowWebhookPayload.class));
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(validPayload);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(webhookReceiverService, times(1)).processWebhook(validPayload);
    }

    @Test
    @DisplayName("POST /webhook - Payload null doit être géré")
    void testReceiveWebhook_NullPayload_HandledGracefully() {
        doThrow(new RuntimeException("Payload invalide"))
            .when(webhookReceiverService).processWebhook(null);
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(null);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /webhook - Payload avec event_id manquant")
    void testReceiveWebhook_MissingEventId_StillProcessed() {
        doNothing().when(webhookReceiverService).processWebhook(any(TomorrowWebhookPayload.class));
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(invalidPayload);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webhookReceiverService, times(1)).processWebhook(invalidPayload);
    }

    // ==================== TESTS GET /alerts ====================

    @Test
    @DisplayName("GET /alerts - Retourne 200 OK")
    void testGetAlerts_ReturnsOk() {
        // Passer null pour plantId pour utiliser findAll()
        ResponseEntity<?> response = weatherWebhookController.getAlerts(null, true);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(weatherAlertRepository, times(1)).findAll();
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(@RequestParam(required = false) String plantId,
                                    @RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            List<WeatherAlert> alerts = new ArrayList<>();  // Initialiser avec une liste vide
            if (plantId != null && !plantId.isEmpty()) {
                List<PlantImpact> impacts = plantImpactRepository.findByPlantIdOrderByTimestampDesc(plantId);
                List<String> alertIds = impacts.stream()
                        .map(PlantImpact::getAlertId)
                        .distinct()
                        .toList();
                if (!alertIds.isEmpty()) {
                    alerts = weatherAlertRepository.findAllById(alertIds);
                }
            } else {
                alerts = weatherAlertRepository.findAll();
                if (alerts == null) {
                    alerts = new ArrayList<>();
                }
            }
            
            if (activeOnly && alerts != null) {
                alerts = alerts.stream().filter(a -> !a.isProcessed()).toList();
            }
            
            if (alerts != null && !alerts.isEmpty()) {
                alerts.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            }
            
            return ResponseEntity.ok(alerts != null ? alerts : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== TESTS DE SÉCURITÉ ====================

    @Test
    @DisplayName("POST /webhook - Payload avec caractères spéciaux")
    void testReceiveWebhook_SpecialCharactersPayload() {
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 38.0);
        details.put("note", "Alerte spéciale !@#$%");
        
        TomorrowWebhookPayload specialPayload = new TomorrowWebhookPayload();
        specialPayload.setEvent_id("test_event_special");
        specialPayload.setType("heatwave");
        specialPayload.setCoords(new double[]{48.8566, 2.3522});
        specialPayload.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        specialPayload.setSeverity("high");
        specialPayload.setDetails(details);
        
        doNothing().when(webhookReceiverService).processWebhook(any(TomorrowWebhookPayload.class));
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(specialPayload);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webhookReceiverService, times(1)).processWebhook(specialPayload);
    }

    @Test
    @DisplayName("POST /webhook - Vérification de la réponse")
    void testReceiveWebhook_ResponseValidation() {
        doNothing().when(webhookReceiverService).processWebhook(any(TomorrowWebhookPayload.class));
        
        ResponseEntity<?> response = weatherWebhookController.receiveWebhook(validPayload);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("DEBUG - Voir ce que retourne getAlerts")
    void testDebugGetAlertsResponse() {
        when(weatherAlertRepository.findAll()).thenReturn(new ArrayList<>());
        
        ResponseEntity<?> response = weatherWebhookController.getAlerts(null, true);
        
        System.err.println("=== RÉPONSE ===");
        System.err.println("Status: " + response.getStatusCode());
        System.err.println("Body: " + response.getBody());
        System.err.println("Body class: " + (response.getBody() != null ? response.getBody().getClass() : "null"));
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}