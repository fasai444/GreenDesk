package org.example.repositories;

import org.example.entites.SensorReading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class SensorReadingRepositoryTest {

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        SensorReading oldReading = new SensorReading("plant-1", now.minusHours(5), 20.0, 40.0, 500.0, 0.0);
        SensorReading newReading = new SensorReading("plant-1", now.minusHours(1), 25.0, 60.0, 1000.0, 5.0);
        SensorReading alertReading = new SensorReading("plant-2", now.minusMinutes(30), 35.0, 20.0, 100.0, 0.0); // Hot, dry, dark

        sensorReadingRepository.saveAll(List.of(oldReading, newReading, alertReading));
    }

    @AfterEach
    void tearDown() {
        sensorReadingRepository.deleteAll();
    }

    @Test
    void testFindByPlantIdOrderByTimestampDesc() {
        List<SensorReading> readings = sensorReadingRepository.findByPlantIdOrderByTimestampDesc("plant-1");
        assertEquals(2, readings.size());
        // Verify order: newest first
        assertTrue(readings.get(0).getTimestamp().isAfter(readings.get(1).getTimestamp()));
        assertEquals(25.0, readings.get(0).getTemperature());
    }

    @Test
    void testFindFirstByPlantIdOrderByTimestampDesc() {
        Optional<SensorReading> latest = sensorReadingRepository.findFirstByPlantIdOrderByTimestampDesc("plant-1");
        assertTrue(latest.isPresent());
        assertEquals(25.0, latest.get().getTemperature());
    }

    @Test
    void testAlertQueries() {
        // Humidity < 30
        List<SensorReading> dry = sensorReadingRepository.findByHumidityLessThan(30.0);
        assertEquals(1, dry.size());
        assertEquals("plant-2", dry.get(0).getPlantId());

        // Temperature > 30
        List<SensorReading> hot = sensorReadingRepository.findByTemperatureGreaterThan(30.0);
        assertEquals(1, hot.size());
        assertEquals("plant-2", hot.get(0).getPlantId());

        // Lux < 200
        List<SensorReading> dark = sensorReadingRepository.findByLuxLessThan(200.0);
        assertEquals(1, dark.size());
        assertEquals("plant-2", dark.get(0).getPlantId());
    }
}