package org.example.entites.environment;

import org.example.entites.plant.Plant;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EnvironmentDataTest {

    @Test
    void testDefaultConstructor() {
        EnvironmentData env = new EnvironmentData();
        
        assertNotNull(env.getTimestamp());
        assertTrue(env.getTemperature() >= 15 && env.getTemperature() <= 30);
        assertTrue(env.getHumidity() >= 30 && env.getHumidity() <= 80);
        assertTrue(env.getLux() >= 0 && env.getLux() <= 2000);
        assertTrue(env.getRainfall() >= 0 && env.getRainfall() <= 10);
        assertNotNull(env.getPlants());
        assertTrue(env.getPlants().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 12, 0);
        EnvironmentData env = new EnvironmentData(time, 22.5, 60.0, 1500.0, 2.5);
        
        assertEquals(time, env.getTimestamp());
        assertEquals(22.5, env.getTemperature());
        assertEquals(60.0, env.getHumidity());
        assertEquals(1500.0, env.getLux());
        assertEquals(2.5, env.getRainfall());
    }

    @Test
    void testEvolveDaytime() {
        // Set time to morning
        LocalDateTime morningTime = LocalDateTime.of(2023, 1, 1, 10, 0); 
        EnvironmentData env = new EnvironmentData(morningTime, 20.0, 50.0, 0.0, 0.0);
        
        env.evolve();
        
        // Time should advance by 1 hour
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 0), env.getTimestamp());
        
        // Daytime checks (11:00 AM is between 6 and 18)
        assertTrue(env.getLux() > 0, "Lux should be greater than 0 during the day");
        assertTrue(env.getTemperature() > 15, "Temperature should be warmer during the day");
        assertTrue(env.getHumidity() >= 0 && env.getHumidity() <= 100, "Humidity must be within 0-100 bounds");
    }

    @Test
    void testEvolveNighttime() {
        // Set time to evening
        LocalDateTime eveningTime = LocalDateTime.of(2023, 1, 1, 20, 0);
        EnvironmentData env = new EnvironmentData(eveningTime, 20.0, 50.0, 1000.0, 0.0);
        
        env.evolve();
        
        assertEquals(LocalDateTime.of(2023, 1, 1, 21, 0), env.getTimestamp());
        
        // Nighttime checks (21:00 is outside 6-18)
        assertEquals(0.0, env.getLux(), "Lux should be 0 at night");
        assertTrue(env.getTemperature() >= 14 && env.getTemperature() <= 16, "Temperature should be cooler (around 15 +- 1)");
    }

    @Test
    void testAddPlant() {
        EnvironmentData env = new EnvironmentData();
        Plant dummyPlant = new Plant(); // Assuming Plant has a default constructor
        
        env.addPlant(dummyPlant);
        
        assertEquals(1, env.getPlants().size());
        assertTrue(env.getPlants().contains(dummyPlant));
    }

    @Test
    void testSetters() {
        EnvironmentData env = new EnvironmentData();
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        
        env.setTimestamp(newTime);
        env.setTemperature(35.0);
        env.setHumidity(90.0);
        env.setLux(3000.0);
        env.setRainfall(15.0);
        
        assertEquals(newTime, env.getTimestamp());
        assertEquals(35.0, env.getTemperature());
        assertEquals(90.0, env.getHumidity());
        assertEquals(3000.0, env.getLux());
        assertEquals(15.0, env.getRainfall());
    }
}