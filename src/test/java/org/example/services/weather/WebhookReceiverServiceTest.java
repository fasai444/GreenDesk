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
import org.example.services.scheduling.CareTaskWeatherRescheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebhookReceiverServiceTest {

    @Mock
    private WeatherAlertRepository weatherAlertRepository;

    @Mock
    private PlantImpactRepository plantImpactRepository;

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private ForestRepository forestRepository;

    @Mock
    private PlantImpactCalculator impactCalculator;

    @Mock
    private PlantStateUpdater stateUpdater;

    @Mock
    private CareTaskWeatherRescheduler careTaskWeatherRescheduler;

    @InjectMocks
    private WebhookReceiverService webhookReceiverService;

    private TomorrowWebhookPayload validPayload;
    private WeatherAlert mockAlert;
    private Plant mockPlant;
    private Forest mockForest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
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
        
        // Créer un mock d'alerte
        mockAlert = mock(WeatherAlert.class);
        when(mockAlert.getId()).thenReturn("alert_123");
        when(mockAlert.getType()).thenReturn("heatwave");
        
        // Créer un mock de plante
        mockPlant = mock(Plant.class);
        when(mockPlant.getId()).thenReturn("plant_123");
        when(mockPlant.getStressIndex()).thenReturn(0.3);
        when(mockPlant.getPlantState()).thenReturn(org.example.entities.plant.PlantState.HEALTHY);
        
        // Créer un mock de forêt
        mockForest = mock(Forest.class);
        when(mockForest.getId()).thenReturn("forest_123");
        when(mockForest.getCoords()).thenReturn(new double[]{48.8566, 2.3522});
    }

    // ==================== TESTS PROCESS WEBHOOK ====================

    /*@Test
    @DisplayName("processWebhook - Nouvelle alerte doit être traitée")
    void testProcessWebhook_NewAlert_ShouldBeProcessed() throws Exception {
        // Given
        when(weatherAlertRepository.findByEventId(validPayload.getEvent_id()))
            .thenReturn(Optional.empty());
        when(weatherAlertRepository.save(any(WeatherAlert.class)))
            .thenReturn(mockAlert);
        when(forestRepository.findAll()).thenReturn(Arrays.asList(mockForest));
        when(plantRepository.findAll()).thenReturn(Arrays.asList(mockPlant));
        when(plantImpactRepository.findByPlantIdOrderByTimestampDesc(mockPlant.getId()))
            .thenReturn(new ArrayList<>());
        when(impactCalculator.calculateISR(any(Plant.class), any(WeatherAlert.class)))
            .thenReturn(0.5);
        when(impactCalculator.calculateSPS(any(Plant.class), anyDouble(), anyList()))
            .thenReturn(0.6);
        when(plantImpactRepository.save(any(PlantImpact.class)))
            .thenReturn(mock(PlantImpact.class));
        
        // When
        webhookReceiverService.processWebhook(validPayload);
        
        // Then
        // save est appelé 2 fois: 1 pour l'alerte initiale, 1 pour marquer traitée
        verify(weatherAlertRepository, times(2)).save(any(WeatherAlert.class));
        verify(plantImpactRepository, times(1)).save(any(PlantImpact.class));
        verify(stateUpdater, times(1)).updatePlantState(any(Plant.class), anyDouble(), anyDouble());
    }*/

    @Test
    @DisplayName("processWebhook - Alerte déjà existante doit être ignorée")
    void testProcessWebhook_DuplicateAlert_ShouldBeIgnored() throws Exception {
        // Given
        when(weatherAlertRepository.findByEventId(validPayload.getEvent_id()))
            .thenReturn(Optional.of(mockAlert));
        
        // When
        webhookReceiverService.processWebhook(validPayload);
        
        // Then
        verify(weatherAlertRepository, never()).save(any(WeatherAlert.class));
        verify(plantImpactRepository, never()).save(any(PlantImpact.class));
        verify(stateUpdater, never()).updatePlantState(any(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("processWebhook - Aucune forêt impactée, aucune plante traitée")
    void testProcessWebhook_NoForestsImpacted_NoPlantsProcessed() throws Exception {
        // Given
        when(weatherAlertRepository.findByEventId(validPayload.getEvent_id()))
            .thenReturn(Optional.empty());
        when(weatherAlertRepository.save(any(WeatherAlert.class)))
            .thenReturn(mockAlert);
        when(forestRepository.findAll()).thenReturn(new ArrayList<>()); // Aucune forêt
        
        // When
        webhookReceiverService.processWebhook(validPayload);
        
        // Then
        verify(plantImpactRepository, never()).save(any(PlantImpact.class));
        verify(stateUpdater, never()).updatePlantState(any(), anyDouble(), anyDouble());
        // save est appelé 2 fois: 1 pour l'alerte initiale, 1 pour marquer traitée
        verify(weatherAlertRepository, times(2)).save(any(WeatherAlert.class));
    }

    @Test
    @DisplayName("processWebhook - Forêt sans coordonnées, ignore la forêt")
    void testProcessWebhook_ForestWithoutCoords_Ignored() throws Exception {
        // Given
        Forest forestWithoutCoords = mock(Forest.class);
        when(forestWithoutCoords.getId()).thenReturn("forest_no_coords");
        when(forestWithoutCoords.getCoords()).thenReturn(null);
        
        when(weatherAlertRepository.findByEventId(validPayload.getEvent_id()))
            .thenReturn(Optional.empty());
        when(weatherAlertRepository.save(any(WeatherAlert.class)))
            .thenReturn(mockAlert);
        when(forestRepository.findAll()).thenReturn(Arrays.asList(forestWithoutCoords));
        
        // When
        webhookReceiverService.processWebhook(validPayload);
        
        // Then
        verify(plantImpactRepository, never()).save(any(PlantImpact.class));
    }

    @Test
    @DisplayName("processWebhook - Plante non sensible à l'alerte, ignorée")
    void testProcessWebhook_PlantNotSensitive_Ignored() throws Exception {
        // Given
        // Configurer une plante non sensible à la chaleur
        org.example.entities.species.Species species = mock(org.example.entities.species.Species.class);
        when(species.isSensitiveToHeat()).thenReturn(false);
        when(mockPlant.getSpecies()).thenReturn(species);
        
        when(weatherAlertRepository.findByEventId(validPayload.getEvent_id()))
            .thenReturn(Optional.empty());
        when(weatherAlertRepository.save(any(WeatherAlert.class)))
            .thenReturn(mockAlert);
        when(forestRepository.findAll()).thenReturn(Arrays.asList(mockForest));
        when(plantRepository.findAll()).thenReturn(Arrays.asList(mockPlant));
        
        // When
        webhookReceiverService.processWebhook(validPayload);
        
        // Then
        verify(impactCalculator, never()).calculateISR(any(), any());
        verify(stateUpdater, never()).updatePlantState(any(), anyDouble(), anyDouble());
    }

    // ==================== TESTS CALCUL DISTANCE ====================

    @Test
    @DisplayName("isWithinRadius - Points identiques, distance 0")
    void testIsWithinRadius_SamePoints_ReturnsTrue() throws Exception {
        double[] point1 = {48.8566, 2.3522};
        double[] point2 = {48.8566, 2.3522};
        double radius = 10.0;
        
        boolean result = invokeIsWithinRadius(point1, point2, radius);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("isWithinRadius - Points dans le rayon")
    void testIsWithinRadius_WithinRadius_ReturnsTrue() throws Exception {
        double[] point1 = {48.8566, 2.3522};
        double[] point2 = {48.8600, 2.3600}; // ~1km de distance
        double radius = 10.0;
        
        boolean result = invokeIsWithinRadius(point1, point2, radius);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("isWithinRadius - Points hors du rayon")
    void testIsWithinRadius_OutsideRadius_ReturnsFalse() throws Exception {
        double[] point1 = {48.8566, 2.3522};
        double[] point2 = {49.0000, 2.5000}; // ~20km de distance
        double radius = 10.0;
        
        boolean result = invokeIsWithinRadius(point1, point2, radius);
        
        assertFalse(result);
    }

    // ==================== TESTS SENSIBILITÉ PLANTE ====================

    @Test
    @DisplayName("isPlantSensitive - Plante sans espèce, retourne true")
    void testIsPlantSensitive_NullSpecies_ReturnsTrue() throws Exception {
        when(mockPlant.getSpecies()).thenReturn(null);
        
        boolean result = invokeIsPlantSensitive(mockPlant, "heatwave");
        
        assertTrue(result);
    }

    @Test
    @DisplayName("isPlantSensitive - Espèce sensible à la chaleur")
    void testIsPlantSensitive_HeatSensitive_ReturnsTrue() throws Exception {
        org.example.entities.species.Species species = mock(org.example.entities.species.Species.class);
        when(species.isSensitiveToHeat()).thenReturn(true);
        when(mockPlant.getSpecies()).thenReturn(species);
        
        boolean result = invokeIsPlantSensitive(mockPlant, "heatwave");
        
        assertTrue(result);
    }

    @Test
    @DisplayName("isPlantSensitive - Espèce non sensible à la chaleur")
    void testIsPlantSensitive_NotHeatSensitive_ReturnsFalse() throws Exception {
        org.example.entities.species.Species species = mock(org.example.entities.species.Species.class);
        when(species.isSensitiveToHeat()).thenReturn(false);
        when(mockPlant.getSpecies()).thenReturn(species);
        
        boolean result = invokeIsPlantSensitive(mockPlant, "heatwave");
        
        assertFalse(result);
    }

    @Test
    @DisplayName("isPlantSensitive - Type d'alerte inconnu, retourne true")
    void testIsPlantSensitive_UnknownAlertType_ReturnsTrue() throws Exception {
        org.example.entities.species.Species species = mock(org.example.entities.species.Species.class);
        when(mockPlant.getSpecies()).thenReturn(species);
        
        boolean result = invokeIsPlantSensitive(mockPlant, "unknown_type");
        
        assertTrue(result);
    }

    // ==================== HELPER METHODS ====================

    private boolean invokeIsWithinRadius(double[] point1, double[] point2, double radiusKm) {
        try {
            java.lang.reflect.Method method = WebhookReceiverService.class.getDeclaredMethod(
                "isWithinRadius", double[].class, double[].class, double.class);
            method.setAccessible(true);
            return (boolean) method.invoke(webhookReceiverService, point1, point2, radiusKm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean invokeIsPlantSensitive(Plant plant, String alertType) {
        try {
            java.lang.reflect.Method method = WebhookReceiverService.class.getDeclaredMethod(
                "isPlantSensitive", Plant.class, String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(webhookReceiverService, plant, alertType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}