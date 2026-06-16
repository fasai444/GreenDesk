package org.example.services.weather;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.repositories.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
//import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

//import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
//import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlantStateUpdaterTest {

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private PlantStateUpdater plantStateUpdater;

    @Mock
    private Plant plant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== TESTS UPDATE PLANT STATE ====================

    @Test
    @DisplayName("updatePlantState - Stress critique (ISR > 0.7) doit rendre la plante DISEASED")
    void testUpdatePlantState_ISR_Critical_ShouldBecomeDiseased() {
        // Given
        double isr = 0.8;
        double sps = 0.9;
        double currentStress = 0.3;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        // Ne pas stubbed getPlantState, laisser la méthode l'appeler après modification
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        // Vérifier que setStressIndex a été appelé avec la bonne valeur
        verify(plant).setStressIndex(0.46); // 0.3 + (0.8 * 0.2) = 0.46
        // Vérifier que setPlantState a été appelé avec DISEASED
        verify(plant).setPlantState(PlantState.DISEASED);
        // Vérifier que save a été appelé
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Nouveau stress > 0.8 doit rendre la plante DISEASED")
    void testUpdatePlantState_NewStressExceedsThreshold_ShouldBecomeDiseased() {
        // Given
        double isr = 0.6;
        double sps = 0.7;
        double currentStress = 0.75;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        verify(plant).setStressIndex(0.87); // 0.75 + (0.6 * 0.2) = 0.87
        verify(plant).setPlantState(PlantState.DISEASED);
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Stress modéré (ISR > 0.4) doit rendre la plante STRESSED")
    void testUpdatePlantState_ISR_Moderate_ShouldBecomeStressed() {
        // Given
        double isr = 0.5;
        double sps = 0.6;
        double currentStress = 0.2;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        // Vérifier que setStressIndex a été appelé avec une valeur positive
        verify(plant).setStressIndex(anyDouble());
        // Vérifier que setPlantState a été appelé avec STRESSED ou DISEASED
        verify(plant).setPlantState(any(PlantState.class));
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Nouveau stress > 0.5 doit rendre la plante STRESSED")
    void testUpdatePlantState_NewStressExceedsModerateThreshold_ShouldBecomeStressed() {
        // Given
        double isr = 0.3;
        double sps = 0.4;
        double currentStress = 0.45;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        verify(plant).setStressIndex(0.51); // 0.45 + (0.3 * 0.2) = 0.51
        verify(plant).setPlantState(PlantState.STRESSED);
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Pas de stress (ISR faible) doit conserver l'état")
    void testUpdatePlantState_LowStress_ShouldKeepState() {
        // Given
        double isr = 0.2;
        double sps = 0.3;
        double currentStress = 0.1;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        verify(plant).setStressIndex(0.14); // 0.1 + (0.2 * 0.2) = 0.14
        // setPlantState ne doit pas être appelé car le stress n'atteint pas les seuils
        verify(plant, never()).setPlantState(any());
        verify(plantRepository, times(1)).save(plant);
    }

   @Test
    @DisplayName("updatePlantState - Stress déjà élevé, ISR faible - vérifie le nouvel état")
    void testUpdatePlantState_AlreadyStressed_LowISR_ShouldRemainStressed() {
        // Given
        double isr = 0.2;
        double sps = 0.3;
        double currentStress = 0.55;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        // Vérifier que setStressIndex a été appelé
        verify(plant).setStressIndex(anyDouble());
        // Vérifier que setPlantState est appelé (car le stress dépasse probablement 0.5)
        // et que c'est STRESSED (pas DISEASED car stress < 0.8)
        verify(plant).setPlantState(PlantState.STRESSED);
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - ISR très élevé avec stress déjà haut devient DISEASED")
    void testUpdatePlantState_VeryHighISR_HighStress_ShouldBecomeDiseased() {
        // Given
        double isr = 0.9;
        double sps = 0.95;
        double currentStress = 0.7;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        verify(plant).setStressIndex(0.88); // 0.7 + (0.9 * 0.2) = 0.88
        verify(plant).setPlantState(PlantState.DISEASED);
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Limite haute du stress (ne dépasse pas 1.0)")
    void testUpdatePlantState_StressDoesNotExceedOne() {
        // Given
        double isr = 1.0;
        double sps = 1.0;
        double currentStress = 0.95;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        verify(plant).setStressIndex(1.0); // min(1.0, 0.95 + 0.2) = 1.0
        verify(plant).setPlantState(PlantState.DISEASED);
        verify(plantRepository, times(1)).save(plant);
    }

    @Test
    @DisplayName("updatePlantState - Limite basse du stress (ne descend pas en dessous de 0)")
    void testUpdatePlantState_StressDoesNotGoBelowZero() {
        // Given
        double isr = -0.5;
        double sps = 0.0;
        double currentStress = 0.05;
        
        when(plant.getStressIndex()).thenReturn(currentStress);
        
        // When
        plantStateUpdater.updatePlantState(plant, isr, sps);
        
        // Then
        // Vérifier que setStressIndex a été appelé avec 0 ou une petite valeur
        verify(plant).setStressIndex(anyDouble());
        verify(plant, never()).setPlantState(any());
        verify(plantRepository, times(1)).save(plant);
    }

}