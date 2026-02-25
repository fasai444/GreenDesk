package org.example.services;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock
    private SensorReadingRepository sensorReadingRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private PlantAlertService plantAlertService;

    @InjectMocks
    private SensorReadingService sensorReadingService;

    private Plant plant;

    @BeforeEach
    void setUp() {
        Species species = new Species("S", 100, 20, 50, 500, 1, 1);
        plant = new Plant("P", species, 100, 20, 50, 500);
    }

    @Test
    void addReading_success_and_notFound() throws Exception {
        CreateSensorReadingRequest req = new CreateSensorReadingRequest();
        req.setTemperature(22);
        req.setHumidity(60);
        req.setLux(350);
        req.setRainfall(5);
        req.setTimestamp(LocalDateTime.now());

        SensorReading saved = new SensorReading("p1", req.getTimestamp(), 22, 60, 350, 5);

        when(plantRepository.findById("p1")).thenReturn(Optional.of(plant));
        when(sensorReadingRepository.save(any(SensorReading.class))).thenReturn(saved);

        SensorReading out = sensorReadingService.addReading("p1", req);
        assertEquals("p1", out.getPlantId());
        verify(plantRepository, atLeastOnce()).save(plant);
        verify(plantAlertService).evaluateAndCreateAlerts(plant);

        when(plantRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> sensorReadingService.addReading("missing", req));
    }

    @Test
    void getters_shouldReturnExpectedData_and_latestThrowWhenMissing() throws Exception {
        SensorReading r = new SensorReading("p1", LocalDateTime.now(), 20, 50, 300, 0);

        when(sensorReadingRepository.findByPlantIdOrderByTimestampDesc("p1")).thenReturn(List.of(r));
        when(sensorReadingRepository.findByPlantIdAndTimestampBetweenOrderByTimestampDesc(any(), any(), any()))
                .thenReturn(List.of(r));
        when(sensorReadingRepository.findFirstByPlantIdOrderByTimestampDesc("p1")).thenReturn(Optional.of(r));
        when(sensorReadingRepository.findFirstByPlantIdOrderByTimestampDesc("none")).thenReturn(Optional.empty());

        assertEquals(1, sensorReadingService.getReadings("p1").size());
        assertEquals(1,
                sensorReadingService.getReadingsBetween("p1", LocalDateTime.now().minusDays(1), LocalDateTime.now())
                        .size());
        assertEquals("p1", sensorReadingService.getLatest("p1").getPlantId());
        assertThrows(Exception.class, () -> sensorReadingService.getLatest("none"));
    }
}
