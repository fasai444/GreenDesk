package org.example.services;

import org.example.entities.SensorReading;
import org.example.entities.alert.AlertSeverity;
import org.example.entities.alert.AlertType;
import org.example.entities.alert.PlantAlert;
import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantEffect;
import org.example.entities.plant.PlantState;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantAlertRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
import org.example.repositories.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreenhouseOpsServiceTest {

    @Mock
    private PlantRepository plantRepository;
    @Mock
    private ForestRepository forestRepository;
    @Mock
    private SpeciesRepository speciesRepository;
    @Mock
    private PlantEffectRepository plantEffectRepository;
    @Mock
    private PlantAlertRepository plantAlertRepository;
    @Mock
    private SensorReadingRepository sensorReadingRepository;
    @Mock
    private SensorReadingService sensorReadingService;

    @InjectMocks
    private GreenhouseOpsService service;

    private Plant healthyPlant;
    private Plant stressedPlant;
    private Forest forest;

    @BeforeEach
    void setup() {
        healthyPlant = new Plant();
        setPlantId(healthyPlant, "p-1");
        healthyPlant.setName("Healthy Plant");
        healthyPlant.setForestId("f-1");
        healthyPlant.setStressIndex(0.1);
        healthyPlant.setPlantState(PlantState.HEALTHY);
        healthyPlant.setTemperature(22.0);
        healthyPlant.setHumidity(55.0);
        healthyPlant.setLux(2500.0);

        stressedPlant = new Plant();
        setPlantId(stressedPlant, "p-2");
        stressedPlant.setName("Stressed Plant");
        stressedPlant.setForestId("f-1");
        stressedPlant.setStressIndex(0.7);
        stressedPlant.setPlantState(PlantState.STRESSED);
        stressedPlant.setTemperature(30.0);
        stressedPlant.setHumidity(30.0);
        stressedPlant.setLux(5000.0);

        forest = new Forest("Serre A", 3, 3);
        forest.setCells(List.of());
        forest.setName("Serre A");
        try {
            var field = Forest.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(forest, "f-1");
        } catch (Exception ignored) {
        }
    }

    @Test
    void getOverview_shouldReturnComputedKpis() {
        when(plantRepository.findAll()).thenReturn(List.of(healthyPlant, stressedPlant));
        when(speciesRepository.count()).thenReturn(3L);
        when(forestRepository.count()).thenReturn(1L);
        when(plantEffectRepository.countByActiveTrue()).thenReturn(2L);
        when(plantAlertRepository.countByAcknowledgedFalse()).thenReturn(4L);
        when(sensorReadingRepository.findByTimestampGreaterThanEqualOrderByTimestampDesc(any()))
                .thenReturn(List.of(new SensorReading(), new SensorReading()));

        Map<String, Object> out = service.getOverview();

        assertEquals(3L, out.get("species"));
        assertEquals(2, out.get("plants"));
        assertEquals(1L, out.get("forests"));
        assertEquals(2L, out.get("activeEffects"));
        assertEquals(4L, out.get("activeAlerts"));
        assertEquals(2, out.get("sensorReadings24h"));
        assertTrue(((Number) out.get("avgStressPct")).doubleValue() > 0);
        assertTrue(((Number) out.get("healthyRatePct")).doubleValue() >= 50);
    }

    @Test
    void getLiveEffectsImpact_shouldFilterAndSortActiveEffects() {
        Plant noForestPlant = new Plant();
        setPlantId(noForestPlant, "p-3");
        noForestPlant.setForestId("");

        PlantEffect fx1 = new PlantEffect("p-1", "e-1", LocalDateTime.now(), 2);
        PlantEffect fx2 = new PlantEffect("p-2", "e-2", LocalDateTime.now(), 2);

        when(plantRepository.findAll()).thenReturn(List.of(healthyPlant, stressedPlant, noForestPlant));
        when(forestRepository.findAll()).thenReturn(List.of(forest));
        when(plantEffectRepository.findByPlantIdAndActive(eq("p-1"), eq(true))).thenReturn(List.of(fx1));
        when(plantEffectRepository.findByPlantIdAndActive(eq("p-2"), eq(true))).thenReturn(List.of(fx1, fx2));

        List<Map<String, Object>> rows = service.getLiveEffectsImpact(10);

        assertEquals(2, rows.size());
        assertEquals("p-2", rows.get(0).get("plantId"));
        assertEquals("Serre A", rows.get(0).get("forestName"));
        assertEquals(2, rows.get(0).get("activeEffectsCount"));
    }

    @Test
    void getRoiInsights_shouldReturnHighRiskRecommendation() {
        healthyPlant.setStressIndex(1.0);
        stressedPlant.setStressIndex(1.0);

        PlantAlert a1 = new PlantAlert("p-1", LocalDateTime.now(), AlertType.HIGH_TEMPERATURE, AlertSeverity.CRITICAL,
                "critical");
        PlantAlert a2 = new PlantAlert("p-2", LocalDateTime.now(), AlertType.LOW_WATER, AlertSeverity.CRITICAL,
                "critical");

        when(plantRepository.findAll()).thenReturn(List.of(healthyPlant, stressedPlant));
        when(plantAlertRepository.findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(a1, a2));
        when(plantEffectRepository.countByActiveTrue()).thenReturn(0L);
        when(sensorReadingRepository.findByTimestampGreaterThanEqualOrderByTimestampDesc(any())).thenReturn(List.of());

        Map<String, Object> out = service.getRoiInsights(24);

        assertTrue(String.valueOf(out.get("recommendation")).startsWith("Risque élevé"));
        assertTrue(((Number) out.get("riskIndex")).doubleValue() >= 70.0);
    }

    @Test
    void getForestRoiRanking_shouldClassifyLevels() {
        Forest f2 = new Forest("Serre B", 3, 3);
        Forest f3 = new Forest("Serre C", 3, 3);
        try {
            var field = Forest.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(f2, "f-2");
            field.set(f3, "f-3");
        } catch (Exception ignored) {
        }

        List<Plant> rentablePlants = new java.util.ArrayList<>();
        for (int index = 0; index < 100; index++) {
            Plant plant = new Plant();
            setPlantId(plant, "rent-" + index);
            plant.setForestId("f-2");
            plant.setStressIndex(0.0);
            plant.setTemperature(20.0);
            plant.setHumidity(50.0);
            plant.setLux(2000.0);
            rentablePlants.add(plant);
        }

        Plant p3 = new Plant();
        setPlantId(p3, "risk-1");
        p3.setForestId("f-3");
        p3.setStressIndex(0.95);
        p3.setTemperature(33.0);
        p3.setHumidity(25.0);
        p3.setLux(6000.0);

        PlantEffect fx = new PlantEffect("rent-0", "e-1", LocalDateTime.now(), 2);

        PlantAlert alert = new PlantAlert("risk-1", LocalDateTime.now(), AlertType.HIGH_TEMPERATURE,
                AlertSeverity.CRITICAL,
                "critical");

        when(forestRepository.findAll()).thenReturn(List.of(forest, f2, f3));
        when(plantRepository.findByForestId("f-1")).thenReturn(List.of(healthyPlant, stressedPlant));
        when(plantRepository.findByForestId("f-2")).thenReturn(rentablePlants);
        when(plantRepository.findByForestId("f-3")).thenReturn(List.of(p3));

        when(plantAlertRepository.findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(alert));
        when(plantEffectRepository.findByPlantIdAndActive(any(), eq(true))).thenAnswer(invocation -> {
            String plantId = invocation.getArgument(0);
            if (String.valueOf(plantId).startsWith("risk-")) {
                return List.of();
            }
            return List.of(fx);
        });

        List<Map<String, Object>> out = service.getForestRoiRanking(10, 24);

        assertEquals(3, out.size());
        assertTrue(out.stream().anyMatch(row -> "RENTABLE".equals(row.get("level"))));
        assertTrue(out.stream().anyMatch(row -> "A_RISQUE".equals(row.get("level"))));
    }

    @Test
    void emitSensorTick_shouldThrowWhenForestNotFound() {
        when(forestRepository.findById("missing")).thenReturn(Optional.empty());
        Exception ex = assertThrows(Exception.class, () -> service.emitSensorTick("missing", "NORMAL"));
        assertTrue(ex.getMessage().contains("Forêt introuvable"));
    }

    @Test
    void emitSensorTick_shouldThrowWhenNoPlants() {
        when(forestRepository.findById("f-1")).thenReturn(Optional.of(forest));
        when(plantRepository.findByForestId("f-1")).thenReturn(List.of());

        Exception ex = assertThrows(Exception.class, () -> service.emitSensorTick("f-1", "NORMAL"));
        assertTrue(ex.getMessage().contains("Aucune plante"));
    }

    @Test
    void emitSensorTick_shouldReturnAggregatesAndHandlePartialFailure() throws Exception {
        when(forestRepository.findById("f-1")).thenReturn(Optional.of(forest));
        when(plantRepository.findByForestId("f-1")).thenReturn(List.of(healthyPlant, stressedPlant));
        when(sensorReadingService.addReading(eq("p-1"), any())).thenReturn(new SensorReading());
        when(sensorReadingService.addReading(eq("p-2"), any())).thenThrow(new RuntimeException("boom"));

        Map<String, Object> out = service.emitSensorTick("f-1", "HOT_DRY");

        assertEquals("f-1", out.get("forestId"));
        assertEquals("HOT_DRY", out.get("profile"));
        assertEquals(2, out.get("targetedPlants"));
        assertEquals(1, out.get("createdReadings"));
        assertEquals(1, out.get("failedReadings"));
        verify(sensorReadingService, times(2)).addReading(any(), any());
    }

    private void setPlantId(Plant plant, String id) {
        try {
            var field = Plant.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(plant, id);
        } catch (Exception ignored) {
        }
    }
}
