package org.example.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SensorReadingTest {

    @Test
    void testNoArgsConstructor() {
        // Utile pour s'assurer que MongoDB/Spring Data pourra instancier la classe
        SensorReading reading = new SensorReading();
        assertNull(reading.getId());
        assertNull(reading.getPlantId());
        assertNull(reading.getTimestamp());
        assertEquals(0.0, reading.getTemperature());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        SensorReading reading = new SensorReading("plant-123", now, 25.5, 60.0, 1500.0, 10.0);

        assertEquals("plant-123", reading.getPlantId());
        assertEquals(now, reading.getTimestamp());
        assertEquals(25.5, reading.getTemperature());
        assertEquals(60.0, reading.getHumidity());
        assertEquals(1500.0, reading.getLux());
        assertEquals(10.0, reading.getRainfall());
        assertNull(reading.getId(), "L'ID devrait être null car non défini dans le constructeur métier");
    }

    @Test
    void testSettersAndGetters() {
        SensorReading reading = new SensorReading();
        LocalDateTime testTime = LocalDateTime.of(2023, 10, 15, 14, 30);

        reading.setId("sensor-id-1");
        reading.setPlantId("plant-999");
        reading.setTimestamp(testTime);
        reading.setTemperature(30.0);
        reading.setHumidity(85.5);
        reading.setLux(2000.0);
        reading.setRainfall(5.0);

        assertEquals("sensor-id-1", reading.getId());
        assertEquals("plant-999", reading.getPlantId());
        assertEquals(testTime, reading.getTimestamp());
        assertEquals(30.0, reading.getTemperature());
        assertEquals(85.5, reading.getHumidity());
        assertEquals(2000.0, reading.getLux());
        assertEquals(5.0, reading.getRainfall());
    }

    @Test
    void testToString() {
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 12, 0);
        SensorReading reading = new SensorReading("plant-1", time, 20.0, 50.0, 1000.0, 0.0);
        reading.setId("id-123");

        String toStringResult = reading.toString();

        assertTrue(toStringResult.contains("id='id-123'"));
        assertTrue(toStringResult.contains("plantId='plant-1'"));
        assertTrue(toStringResult.contains("temperature=20.0"));
        assertTrue(toStringResult.contains("humidity=50.0"));
        assertTrue(toStringResult.contains("lux=1000.0"));
        assertTrue(toStringResult.contains("rainfall=0.0"));
    }
}