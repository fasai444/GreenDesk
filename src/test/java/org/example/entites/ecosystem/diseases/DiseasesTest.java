package org.example.entites.ecosystem.diseases;

import org.example.entites.plant.Plant;
import org.example.entites.plant.PlantState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiseasesTest {

    @Test
    void testBacterialDiseaseProgress() {
        Plant mockPlant = mock(Plant.class);
        when(mockPlant.getWaterLevel()).thenReturn(100.0);
        when(mockPlant.getStressIndex()).thenReturn(0.5);

        BacterialDisease disease = new BacterialDisease(0.5);
        disease.progress(mockPlant);

        // Vérification mathématique (0.1 * 0.5 = 0.05 perte de ratio)
        verify(mockPlant).setWaterLevel(95.0); 
        verify(mockPlant).setStressIndex(0.55);

        // La sévérité augmente de 0.03 + [0, 0.02]
        assertTrue(disease.getSeverity() >= 0.53 && disease.getSeverity() <= 0.55);
        
        // A dépassé 0.5, donc passe en DISEASED
        verify(mockPlant).setPlantState(PlantState.DISEASED);
        
        // Vérification des constantes
        assertEquals("BacterialWilt", disease.getName());
        assertEquals(0.7, disease.getInfectionThreshold());
        
        PlantDisease copy = disease.copy();
        assertEquals(disease.getSeverity(), copy.getSeverity());
    }

    @Test
    void testMildiouDiseaseProgress() {
        Plant mockPlant = mock(Plant.class);
        when(mockPlant.getStressIndex()).thenReturn(0.2);
        when(mockPlant.getHeightCm()).thenReturn(100.0);

        MildiouDisease mildiou = new MildiouDisease(0.4);
        mildiou.progress(mockPlant);

        verify(mockPlant).setStressIndex(0.2 + (0.04 * 0.4));
        verify(mockPlant).setHeightCm(100.0 * (1 - 0.025 * 0.4));

        assertTrue(mildiou.getSeverity() >= 0.42 && mildiou.getSeverity() <= 0.43);
        verify(mockPlant).setPlantState(PlantState.STRESSED); // Entre 0.3 et 0.6

        PlantDisease copy = mildiou.copy();
        assertTrue(copy instanceof MildiouDisease);
    }

    @Test
    void testRustDiseaseProgress() {
        Plant mockPlant = mock(Plant.class);
        when(mockPlant.getStressIndex()).thenReturn(0.1);
        when(mockPlant.getHeightCm()).thenReturn(50.0);

        RustDisease rust = new RustDisease(0.8);
        rust.progress(mockPlant);

        // Stress est capé à 1.0 (0.1 + 0.03*0.8 = 0.124)
        verify(mockPlant).setStressIndex(0.124);
        
        assertTrue(rust.getSeverity() >= 0.815 && rust.getSeverity() <= 0.825);
        
        // Supérieur à 0.7, donc DISEASED
        verify(mockPlant).setPlantState(PlantState.DISEASED);
    }
}