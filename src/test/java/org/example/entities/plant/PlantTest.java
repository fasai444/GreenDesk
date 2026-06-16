package org.example.entities.plant;

import org.example.entities.species.Species;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlantTest {

    private Species mockSpecies;

    @BeforeEach
    void setUp() {
        mockSpecies = Mockito.mock(Species.class);
        // Setup default optimal values for the mock species
        when(mockSpecies.getOptimalWaterNeeds()).thenReturn(100.0);
        when(mockSpecies.getOptimalTemperature()).thenReturn(22.0);
        when(mockSpecies.getOptimalHumidity()).thenReturn(50.0);
        when(mockSpecies.getOptimalLuxNeeds()).thenReturn(1000.0);
        when(mockSpecies.lightStressFactor(Mockito.anyDouble())).thenReturn(0.0); // Assume no light stress initially
    }

    @Test
    void testDefaultConstructor() {
        Plant plant = new Plant();
        assertNull(plant.getName());
        assertNull(plant.getSpecies());
    }

    @Test
    void testConstructorWithEnvironmentData() {
        // Creating plant exactly at optimal conditions
        Plant plant = new Plant("TestPlant", mockSpecies, 100.0, 22.0, 50.0, 1000.0);
        
        assertEquals("TestPlant", plant.getName());
        assertEquals(mockSpecies, plant.getSpecies());
        assertEquals(100.0, plant.getWaterLevel());
        assertEquals(22.0, plant.getTemperature());
        assertEquals(50.0, plant.getHumidity());
        assertEquals(1000.0, plant.getLux());
        
        assertEquals(0.0, plant.getStressIndex(), 0.01); // 0 stress at optimal
        assertEquals(PlantState.HEALTHY, plant.getPlantState());
        assertEquals(0.0, plant.getHeightCm());
        assertTrue(plant.getVariationSeed() >= 0 && plant.getVariationSeed() < 1000000);
    }
    
    @Test
    void testConstructorForTests() {
         // This constructor generates random values around the optimal
         Plant plant = new Plant("TestRandomPlant", mockSpecies);
         
         assertEquals("TestRandomPlant", plant.getName());
         assertEquals(PlantState.HEALTHY, plant.getPlantState());
         assertEquals(0.0, plant.getStressIndex());
         // We can't strictly assert the random values, but we can verify they fall within the bounds defined in constructor
         assertTrue(plant.getWaterLevel() >= 85.0 && plant.getWaterLevel() <= 115.0);
         assertTrue(plant.getTemperature() >= 18.0 && plant.getTemperature() <= 26.0);
         assertTrue(plant.getHumidity() >= 40.0 && plant.getHumidity() <= 60.0);
         assertTrue(plant.getLux() >= 900.0 && plant.getLux() <= 1100.0);
    }

    @Test
    void testEvaluateStateHealthy() {
        Plant plant = new Plant("Test", mockSpecies, 100.0, 22.0, 50.0, 1000.0); // Optimal
        assertEquals(PlantState.HEALTHY, plant.evaluateState());
    }

    @Test
    void testEvaluateStateStressed() {
        // Create conditions that cause moderate stress
        // Temperature diff: 22 - 18 = 4. Stress = 4/10 = 0.4
        Plant plant = new Plant("Test", mockSpecies, 100.0, 18.0, 50.0, 1000.0); 
        assertEquals(PlantState.STRESSED, plant.evaluateState());
        assertTrue(plant.getStressIndex() >= 0.3 && plant.getStressIndex() < 0.6);
    }
    
    @Test
    void testEvaluateStateDormant() {
        // Create conditions that cause high stress
        // Water diff: 50. Stress = 50/100 = 0.5
        // Temp diff: 22 - 18 = 4. Stress = 0.4
        // Total stress ~ 0.9
        Plant plant = new Plant("Test", mockSpecies, 60.0, 18.0, 50.0, 1000.0);
        assertEquals(PlantState.DORMANT, plant.evaluateState());
        assertTrue(plant.getStressIndex() >= 0.6 && plant.getStressIndex() < 0.9);
    }

    @Test
    void testEvaluateStateDiseased() {
        // Create conditions that cause extreme stress (all factors bad)
        when(mockSpecies.lightStressFactor(Mockito.anyDouble())).thenReturn(1.0); // Max light stress
        Plant plant = new Plant("Test", mockSpecies, 0.0, 0.0, 0.0, 0.0); 
        assertEquals(PlantState.DISEASED, plant.evaluateState());
        assertEquals(1.0, plant.getStressIndex()); // Stress capped at 1.0
    }

    @Test
    void testSettersAndGetters() {
        Plant plant = new Plant();
        
        plant.setName("NewName");
        plant.setSpecies(mockSpecies);
        plant.setWaterLevel(10.5);
        plant.setTemperature(15.0);
        plant.setHumidity(80.0);
        plant.setLux(500.0);
        plant.setStressIndex(0.2);
        plant.setPlantState(PlantState.STRESSED);
        plant.setHeightCm(12.5);
        
        plant.setForestId("forest-1");
        plant.setX(1);
        plant.setY(2);
        plant.setVariationSeed(42);

        assertEquals("NewName", plant.getName());
        assertEquals(mockSpecies, plant.getSpecies());
        assertEquals(10.5, plant.getWaterLevel());
        assertEquals(15.0, plant.getTemperature());
        assertEquals(80.0, plant.getHumidity());
        assertEquals(500.0, plant.getLux());
        assertEquals(0.2, plant.getStressIndex());
        assertEquals(PlantState.STRESSED, plant.getPlantState());
        assertEquals(12.5, plant.getHeightCm());
        assertEquals("forest-1", plant.getForestId());
        assertEquals(1, plant.getX());
        assertEquals(2, plant.getY());
        assertEquals(42, plant.getVariationSeed());
        assertNull(plant.getId());
    }
}
