package org.example.services;

import org.example.entities.alert.AlertSeverity;
import org.example.entities.alert.AlertType;
import org.example.entities.alert.PlantAlert;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.PlantAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantAlertServiceTest {

    @Mock
    private PlantAlertRepository plantAlertRepository;

    @InjectMocks
    private PlantAlertService plantAlertService;

    private Plant plant;

    @BeforeEach
    void setUp() {
        Species species = new Species("Spec", 100, 20, 60, 400, 1, 1);
        plant = new Plant("P", species, 40, 35, 30, 100);
    }

    @Test
    void getAlertsForPlant_and_acknowledge() throws Exception {
        PlantAlert a = new PlantAlert("p1", LocalDateTime.now(), AlertType.LOW_WATER, AlertSeverity.WARNING, "m");
        a.setId("a1");

        when(plantAlertRepository.findByPlantIdAndAcknowledgedFalseOrderByCreatedAtDesc("p1")).thenReturn(List.of(a));
        when(plantAlertRepository.findByPlantIdOrderByCreatedAtDesc("p1")).thenReturn(List.of(a));
        when(plantAlertRepository.findById("a1")).thenReturn(Optional.of(a));
        when(plantAlertRepository.save(any(PlantAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(1, plantAlertService.getAlertsForPlant("p1", true).size());
        assertEquals(1, plantAlertService.getAlertsForPlant("p1", false).size());

        PlantAlert ack = plantAlertService.acknowledge("a1");
        assertTrue(ack.isAcknowledged());

        when(plantAlertRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> plantAlertService.acknowledge("missing"));
    }

    @Test
    void evaluateAndCreateAlerts_shouldCreateAlerts_whenNoRecentDuplicate() {
        when(plantAlertRepository.findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(plantAlertRepository.save(any(PlantAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PlantAlert> created = plantAlertService.evaluateAndCreateAlerts(plant);

        assertFalse(created.isEmpty());
        verify(plantAlertRepository, atLeastOnce()).save(any(PlantAlert.class));
    }

    @Test
    void evaluateAndCreateAlerts_shouldDeduplicateRecentAlerts() {
        PlantAlert recent = new PlantAlert("p1", LocalDateTime.now(), AlertType.HIGH_TEMPERATURE,
                AlertSeverity.WARNING, "recent");

        when(plantAlertRepository.findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(eq("p1"),
                eq(AlertType.HIGH_TEMPERATURE))).thenReturn(Optional.of(recent));
        when(plantAlertRepository.findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(eq("p1"),
                eq(AlertType.LOW_HUMIDITY))).thenReturn(Optional.empty());
        when(plantAlertRepository.findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(eq("p1"),
                eq(AlertType.LOW_LIGHT))).thenReturn(Optional.empty());
        when(plantAlertRepository.findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(eq("p1"),
                eq(AlertType.LOW_WATER))).thenReturn(Optional.empty());
        when(plantAlertRepository.save(any(PlantAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PlantAlert> created = plantAlertService.evaluateAndCreateAlerts(plant);
        assertTrue(created.stream().noneMatch(a -> a.getType() == AlertType.HIGH_TEMPERATURE));
    }
}
