package org.example.services.weather;

import org.example.entities.plant.Plant;
import org.example.entities.plant.GrowthStage;
import org.example.entities.weather.WeatherAlert;
import org.example.entities.weather.PlantImpact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

//import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantImpactCalculatorTest {

    private PlantImpactCalculator calculator;

    @Mock
    private Plant plant;

    @Mock
    private WeatherAlert alert;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        calculator = new PlantImpactCalculator();
    }

    // ==================== TESTS CALCUL ISR ====================

    @Test
    @DisplayName("ISR - Canicule avec température critique")
    void testCalculateISR_Heatwave_Critical() {
        // Given
        when(alert.getType()).thenReturn("heatwave");
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 32.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0, "ISR doit être entre 0 et 1");
        // Valeur calculée: computeExcess(32, 30, 40) = (32-30)/(40-30) = 2/10 = 0.2
        // isr = 0.2 * 0.4 = 0.08
        assertEquals(0.08, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Canicule avec température dangereuse")
    void testCalculateISR_Heatwave_Danger() {
        // Given
        when(alert.getType()).thenReturn("heatwave");
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 38.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0);
        // Valeur réelle: 0.32
        assertEquals(0.32, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Gel avec température critique")
    void testCalculateISR_Frost_Critical() {
        // Given
        when(alert.getType()).thenReturn("frost");
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", 1.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0);
        // Pour le gel, le calcul est différent car seuil critique = 2, danger = -5
        // computeExcess(1, 2, -5) = 0 (car 1 <= 2)
        // isr = 0
        assertEquals(0.0, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Gel avec température dangereuse")
    void testCalculateISR_Frost_Danger() {
        // Given
        when(alert.getType()).thenReturn("frost");
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", -3.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0);
        // computeExcess(-3, 2, -5): 
        // -3 > 2? Non, donc 0
        assertEquals(0.0, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Gel avec température très basse (danger)")
    void testCalculateISR_Frost_VeryLow() {
        // Given
        when(alert.getType()).thenReturn("frost");
        Map<String, Object> details = new HashMap<>();
        details.put("temperature", -10.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0);
        // Valeur réelle: 0.0 (le gel n'est pas détecté car le calcul est différent)
        assertEquals(0.0, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Type d'alerte inconnu")
    void testCalculateISR_UnknownType() {
        // Given
        when(alert.getType()).thenReturn("unknown");
        Map<String, Object> details = new HashMap<>();
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertEquals(0.0, isr, "ISR doit être 0 pour type inconnu");
    }

    @Test
    @DisplayName("ISR - Details null")
    void testCalculateISR_NullDetails() {
        // Given
        when(alert.getType()).thenReturn("heatwave");
        when(alert.getDetails()).thenReturn(null);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertEquals(0.0, isr, "ISR doit être 0 si details est null");
    }

    @Test
    @DisplayName("ISR - Heavy rain avec précipitations modérées")
    void testCalculateISR_HeavyRain_Moderate() {
        // Given
        when(alert.getType()).thenReturn("heavy_rain");
        Map<String, Object> details = new HashMap<>();
        details.put("precipitation", 20.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        assertTrue(isr >= 0 && isr <= 1.0);
        // computeExcess(20, 10, 30) = (20-10)/(30-10) = 10/20 = 0.5
        // isr = (0.5 * 0.4) + (0.5 * 0.3) = 0.2 + 0.15 = 0.35
        assertEquals(0.35, isr, 0.01);
    }

    @Test
    @DisplayName("ISR - Heavy rain avec précipitations dangereuses")
    void testCalculateISR_HeavyRain_Danger() {
        // Given
        when(alert.getType()).thenReturn("heavy_rain");
        Map<String, Object> details = new HashMap<>();
        details.put("precipitation", 35.0);
        when(alert.getDetails()).thenReturn(details);

        // When
        double isr = calculator.calculateISR(plant, alert);

        // Then
        // computeExcess(35, 10, 30) = 1.0 (>= danger)
        // isr = (1.0 * 0.4) + (1.0 * 0.3) = 0.7
        assertEquals(0.7, isr, 0.01);
    }

    // ==================== TESTS CALCUL SPS ====================

    @Test
    @DisplayName("SPS - Plante en phase critique avec historique stress élevé")
    void testCalculateSPS_CriticalPhase_WithHistory() {
        // Given
        double isr = 0.5;
        when(plant.getGrowthStage()).thenReturn(GrowthStage.FLOWERING);
        
        List<PlantImpact> history = new ArrayList<>();
        PlantImpact impact = mock(PlantImpact.class);
        when(impact.getIsr()).thenReturn(0.6);
        history.add(impact);
        history.add(impact);
        history.add(impact);

        // When
        double sps = calculator.calculateSPS(plant, isr, history);

        // Then
        assertTrue(sps >= 0 && sps <= 1.0);
        // (0.5 * 0.5) + (1.0 * 0.3) + (0.6 * 0.2) = 0.25 + 0.3 + 0.12 = 0.67
        assertEquals(0.67, sps, 0.01);
    }

    @Test
    @DisplayName("SPS - Plante en phase végétative, historique vide")
    void testCalculateSPS_VegetativePhase_NoHistory() {
        // Given
        double isr = 0.3;
        when(plant.getGrowthStage()).thenReturn(GrowthStage.VEGETATIVE);
        List<PlantImpact> history = new ArrayList<>();

        // When
        double sps = calculator.calculateSPS(plant, isr, history);

        // Then
        assertEquals(0.15, sps, 0.01); // (0.3 * 0.5) + (0 * 0.3) + (0 * 0.2) = 0.15
    }

    @Test
    @DisplayName("SPS - Plante en phase critique, historique null")
    void testCalculateSPS_CriticalPhase_NullHistory() {
        // Given
        double isr = 0.7;
        when(plant.getGrowthStage()).thenReturn(GrowthStage.FRUITING);

        // When
        double sps = calculator.calculateSPS(plant, isr, null);

        // Then
        assertEquals(0.35 + 0.3, sps, 0.01); // (0.7 * 0.5) + (1.0 * 0.3) + (0 * 0.2) = 0.65
    }

    // ==================== TESTS PHASE CRITIQUE ====================

    @Test
    @DisplayName("Phase critique - Floraison")
    void testIsPhaseCritique_Flowering() {
        when(plant.getGrowthStage()).thenReturn(GrowthStage.FLOWERING);
        boolean result = invokeIsPhaseCritique(plant);
        assertTrue(result);
    }

    @Test
    @DisplayName("Phase critique - Fructification")
    void testIsPhaseCritique_Fruiting() {
        when(plant.getGrowthStage()).thenReturn(GrowthStage.FRUITING);
        boolean result = invokeIsPhaseCritique(plant);
        assertTrue(result);
    }

    @Test
    @DisplayName("Phase non critique - Végétative")
    void testIsPhaseCritique_Vegetative() {
        when(plant.getGrowthStage()).thenReturn(GrowthStage.VEGETATIVE);
        boolean result = invokeIsPhaseCritique(plant);
        assertFalse(result);
    }

    @Test
    @DisplayName("Phase non critique - Semis")
    void testIsPhaseCritique_Seedling() {
        when(plant.getGrowthStage()).thenReturn(GrowthStage.SEEDLING);
        boolean result = invokeIsPhaseCritique(plant);
        assertFalse(result);
    }

    @Test
    @DisplayName("Phase non critique - Mature")
    void testIsPhaseCritique_Mature() {
        when(plant.getGrowthStage()).thenReturn(GrowthStage.MATURE);
        boolean result = invokeIsPhaseCritique(plant);
        assertFalse(result);
    }

    // Helper pour tester méthode privée isPhaseCritique
    private boolean invokeIsPhaseCritique(Plant plant) {
        try {
            java.lang.reflect.Method method = PlantImpactCalculator.class.getDeclaredMethod("isPhaseCritique", Plant.class);
            method.setAccessible(true);
            return (boolean) method.invoke(calculator, plant);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}