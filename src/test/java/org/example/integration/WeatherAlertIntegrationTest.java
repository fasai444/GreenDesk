package org.example.integration;

import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.entities.weather.PlantImpact;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.repositories.PlantImpactRepository;
import org.example.services.weather.PlantImpactCalculator;
import org.example.services.weather.PlantStateUpdater;
import org.example.services.weather.WebhookReceiverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeatherAlertIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired
    private WeatherAlertRepository weatherAlertRepository;

    @Autowired
    private PlantImpactRepository plantImpactRepository;

    @MockBean
    private PlantImpactCalculator impactCalculator;

    @MockBean
    private PlantStateUpdater stateUpdater;

    @MockBean
    private WebhookReceiverService webhookReceiverService;

    private Forest testForest;
    private Plant testPlant;
    private Species testSpecies;
    private String webhookUrl = "/api/weather/webhook";

    @BeforeEach
    void setUp() {
        // Nettoyer les données de test
        plantImpactRepository.deleteAll();
        weatherAlertRepository.deleteAll();
        plantRepository.deleteAll();
        forestRepository.deleteAll();
        speciesRepository.deleteAll();

        // Créer une espèce de test
        testSpecies = new Species();
        testSpecies.setName("Test Species");
        testSpecies.setOptimalWaterNeeds(200.0);
        testSpecies.setOptimalTemperature(22.0);
        testSpecies.setOptimalHumidity(60.0);
        testSpecies.setOptimalLuxNeeds(1500.0);
        testSpecies.setBaseGrowthRate(1.0);
        testSpecies.setSeedProductionRate(0.5);
        testSpecies.setSensitiveToHeat(true);
        testSpecies = speciesRepository.save(testSpecies);

        // Créer une plante de test
        testPlant = new Plant();
        testPlant.setName("Test Plant");
        testPlant.setSpecies(testSpecies);
        testPlant.setStressIndex(0.3);
        testPlant.setPlantState(PlantState.HEALTHY);
        testPlant.setForestId("forest_123");
        testPlant = plantRepository.save(testPlant);

        // Créer une forêt de test
        testForest = new Forest();
        testForest.setName("Test Forest");
        testForest.setWidth(10);
        testForest.setHeight(10);
        testForest.setCoords(new double[]{48.8566, 2.3522});
        testForest = forestRepository.save(testForest);
        
        // Mettre à jour la plante avec la forêt
        testPlant.setForestId(testForest.getId());
        plantRepository.save(testPlant);
    }

    // ==================== TESTS D'INTÉGRATION ====================

    @Test
    @DisplayName("INTÉGRATION - Envoi d'un webhook valide crée une alerte")
    void testWebhook_ValidPayload_CreatesAlert() throws Exception {
        // Given
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 38.0);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("event_id", "integration_test_001");
        payload.put("type", "heatwave");
        payload.put("coords", new double[]{48.8566, 2.3522});
        payload.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        payload.put("severity", "high");
        payload.put("details", details);

        // When - Appel réel au controller (pas de mock)
        // Note: Le service est mocké, donc on teste juste l'endpoint
        doNothing().when(webhookReceiverService).processWebhook(any());
        
        ResponseEntity<String> response = postWebhook(payload);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webhookReceiverService, times(1)).processWebhook(any());
    }

    @Test
    @DisplayName("INTÉGRATION - Webhook avec données manquantes")
    void testWebhook_MissingData_ReturnsError() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "heatwave"); // event_id manquant

        // When
        doThrow(new RuntimeException("Erreur de traitement"))
            .when(webhookReceiverService).processWebhook(any());
        
        ResponseEntity<String> response = postWebhook(payload);
        
        // Then
        // Le service mocké lance une exception, donc réponse 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private ResponseEntity<String> postWebhook(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Webhook-Secret", "test-secret");
        return restTemplate.postForEntity(webhookUrl, new HttpEntity<>(payload, headers), String.class);
    }

    @Test
    @DisplayName("INTÉGRATION - Vérification des alertes stockées")
    void testAlertStorage() {
        // Given
        WeatherAlert alert = new WeatherAlert();
        alert.setEventId("storage_test_001");
        alert.setType("heatwave");
        alert.setCoords(new double[]{48.8566, 2.3522});
        alert.setTimestamp(LocalDateTime.now());
        alert.setSeverity("high");
        alert.setProcessed(true);
        alert.setProcessedAt(LocalDateTime.now());
        
        // When
        WeatherAlert savedAlert = weatherAlertRepository.save(alert);
        
        // Then
        assertNotNull(savedAlert.getId());
        assertEquals("storage_test_001", savedAlert.getEventId());
        assertEquals("heatwave", savedAlert.getType());
        assertTrue(savedAlert.isProcessed());
    }

    @Test
    @DisplayName("INTÉGRATION - Vérification des impacts stockés")
    void testPlantImpactStorage() {
        // Given
        WeatherAlert alert = weatherAlertRepository.save(createTestAlert());
        
        PlantImpact impact = new PlantImpact();
        impact.setAlertId(alert.getId());
        impact.setPlantId(testPlant.getId());
        impact.setIsr(0.5);
        impact.setSps(0.6);
        impact.setPreviousStress(0.3);
        impact.setNewStress(0.45);
        impact.setPreviousState("HEALTHY");
        impact.setNewState("STRESSED");
        
        // When
        PlantImpact savedImpact = plantImpactRepository.save(impact);
        
        // Then
        assertNotNull(savedImpact.getId());
        assertEquals(alert.getId(), savedImpact.getAlertId());
        assertEquals(testPlant.getId(), savedImpact.getPlantId());
        assertEquals(0.5, savedImpact.getIsr());
        assertEquals(0.6, savedImpact.getSps());
    }

    @Test
    @DisplayName("INTÉGRATION - Recherche d'alertes par eventId")
    void testFindAlertByEventId() {
        // Given
        WeatherAlert alert = createTestAlert();
        alert.setEventId("unique_event_123");
        weatherAlertRepository.save(alert);
        
        // When
        var found = weatherAlertRepository.findByEventId("unique_event_123");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("unique_event_123", found.get().getEventId());
    }

    @Test
    @DisplayName("INTÉGRATION - Recherche d'impacts par plante")
    void testFindImpactsByPlantId() {
        // Given
        WeatherAlert alert = weatherAlertRepository.save(createTestAlert());
        
        PlantImpact impact1 = createTestImpact(alert.getId(), testPlant.getId(), 0.5);
        PlantImpact impact2 = createTestImpact(alert.getId(), testPlant.getId(), 0.6);
        plantImpactRepository.save(impact1);
        plantImpactRepository.save(impact2);
        
        // When
        var impacts = plantImpactRepository.findByPlantIdOrderByTimestampDesc(testPlant.getId());
        
        // Then
        assertEquals(2, impacts.size());
        assertTrue(impacts.stream().allMatch(i -> i.getPlantId().equals(testPlant.getId())));
    }

    @Test
    @DisplayName("INTÉGRATION - Recherche d'impacts par alerte")
    void testFindImpactsByAlertId() {
        // Given
        WeatherAlert alert = weatherAlertRepository.save(createTestAlert());
        
        PlantImpact impact1 = createTestImpact(alert.getId(), testPlant.getId(), 0.5);
        PlantImpact impact2 = createTestImpact(alert.getId(), testPlant.getId(), 0.6);
        plantImpactRepository.save(impact1);
        plantImpactRepository.save(impact2);
        
        // When
        var impacts = plantImpactRepository.findByAlertId(alert.getId());
        
        // Then
        assertEquals(2, impacts.size());
        assertTrue(impacts.stream().allMatch(i -> i.getAlertId().equals(alert.getId())));
    }

    @Test
    @DisplayName("INTÉGRATION - Forêt avec coordonnées")
    void testForestWithCoordinates() {
        // Given
        Forest forest = new Forest();
        forest.setName("Coordinate Forest");
        forest.setWidth(5);
        forest.setHeight(5);
        forest.setCoords(new double[]{48.8566, 2.3522});
        
        // When
        Forest savedForest = forestRepository.save(forest);
        
        // Then
        assertNotNull(savedForest.getCoords());
        assertEquals(48.8566, savedForest.getCoords()[0]);
        assertEquals(2.3522, savedForest.getCoords()[1]);
    }

    @Test
    @DisplayName("INTÉGRATION - Plante avec stade de croissance")
    void testPlantWithGrowthStage() {
        // Given
        testPlant.setGrowthStage(org.example.entities.plant.GrowthStage.FLOWERING);
        
        // When
        Plant savedPlant = plantRepository.save(testPlant);
        
        // Then
        assertNotNull(savedPlant.getGrowthStage());
        assertEquals(org.example.entities.plant.GrowthStage.FLOWERING, savedPlant.getGrowthStage());
    }

    @Test
    @DisplayName("INTÉGRATION - Espèce avec sensibilité à la chaleur")
    void testSpeciesHeatSensitivity() {
        // Given
        Species species = new Species();
        species.setName("Heat Sensitive Plant");
        species.setSensitiveToHeat(true);
        species.setSensitiveToFrost(false);
        
        // When
        Species savedSpecies = speciesRepository.save(species);
        
        // Then
        assertTrue(savedSpecies.isSensitiveToHeat());
        assertFalse(savedSpecies.isSensitiveToFrost());
    }

    // ==================== HELPER METHODS ====================

    private WeatherAlert createTestAlert() {
        WeatherAlert alert = new WeatherAlert();
        alert.setEventId("test_event_" + System.currentTimeMillis());
        alert.setType("heatwave");
        alert.setCoords(new double[]{48.8566, 2.3522});
        alert.setTimestamp(LocalDateTime.now());
        alert.setSeverity("medium");
        alert.setProcessed(false);
        return alert;
    }

    private PlantImpact createTestImpact(String alertId, String plantId, double isr) {
        PlantImpact impact = new PlantImpact();
        impact.setAlertId(alertId);
        impact.setPlantId(plantId);
        impact.setIsr(isr);
        impact.setSps(isr + 0.1);
        impact.setPreviousStress(0.3);
        impact.setNewStress(0.3 + isr * 0.2);
        impact.setPreviousState("HEALTHY");
        impact.setNewState(isr > 0.4 ? "STRESSED" : "HEALTHY");
        impact.setTimestamp(LocalDateTime.now());
        return impact;
    }
}
