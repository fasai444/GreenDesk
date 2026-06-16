package org.example.services;

import org.example.entities.Stimulus;
import org.example.entities.effect.Effect;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.StimulusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StimulusServiceTest {

    @Mock
    private StimulusRepository stimulusRepository;
    @Mock
    private PlantRepository plantRepository;
    @Mock
    private EffectService effectService;
    @Mock
    private EffectRepository effectRepository;

    @InjectMocks
    private StimulusService stimulusService;

    private Stimulus stimulus;

    @BeforeEach
    void setUp() {
        stimulus = new Stimulus("HEATWAVE", "forest-1", 3.0, 6);
    }

    @Test
    void applyToForest_shouldValidateInput() {
        assertThrows(NullPointerException.class, () -> stimulusService.applyToForest(null));

        Stimulus s1 = new Stimulus("HEATWAVE", "", 1.0, 1);
        assertThrows(IllegalArgumentException.class, () -> stimulusService.applyToForest(s1));

        Stimulus s2 = new Stimulus("", "forest-1", 1.0, 1);
        assertThrows(IllegalArgumentException.class, () -> stimulusService.applyToForest(s2));

        Stimulus s3 = new Stimulus("RAIN", "forest-1", 1.0, 0);
        assertThrows(IllegalArgumentException.class, () -> stimulusService.applyToForest(s3));
    }

    @Test
    void applyToForest_shouldReturnImmediatelyWhenNoPlants() throws Exception {
        when(stimulusRepository.save(any(Stimulus.class))).thenReturn(stimulus);
        when(plantRepository.findByForestId("forest-1")).thenReturn(List.of());

        Stimulus out = stimulusService.applyToForest(stimulus);

        assertSame(stimulus, out);
        verify(effectRepository, never()).save(any(Effect.class));
        verify(effectService, never()).applyEffectToPlant(any(), any());
    }

    @Test
    void applyToForest_heatwave_and_rain_shouldUpdatePlants() throws Exception {
        Species species = new Species("S", 100, 20, 50, 500, 1, 1);
        Plant p = spy(new Plant("P", species, 100, 20, 50, 500));
        doReturn("p1").when(p).getId();

        when(stimulusRepository.save(any(Stimulus.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.findByForestId("forest-1")).thenReturn(List.of(p));

        Effect savedHeat = mock(Effect.class);
        when(savedHeat.getId()).thenReturn("e1");
        when(effectRepository.save(any(Effect.class))).thenReturn(savedHeat);

        Stimulus heat = new Stimulus("HEATWAVE", "forest-1", 4.0, 6);
        stimulusService.applyToForest(heat);

        verify(effectService).applyEffectToPlant("p1", "e1");
        verify(plantRepository, atLeastOnce()).save(eq(p));

        Effect savedRain = mock(Effect.class);
        when(savedRain.getId()).thenReturn("e2");
        when(effectRepository.save(any(Effect.class))).thenReturn(savedRain);

        Stimulus rain = new Stimulus("RAIN", "forest-1", 5.0, 6);
        stimulusService.applyToForest(rain);

        verify(effectService).applyEffectToPlant("p1", "e2");
    }

    @Test
    void clonePlantToForest_shouldValidate_and_clone() throws Exception {
        assertThrows(NullPointerException.class, () -> stimulusService.clonePlantToForest(null, "f", 1, 1));
        assertThrows(NullPointerException.class, () -> stimulusService.clonePlantToForest("p", null, 1, 1));

        when(plantRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> stimulusService.clonePlantToForest("missing", "f1", 1, 1));

        Species species = new Species("S", 100, 20, 50, 500, 1, 1);
        Plant original = new Plant("Orig", species, 100, 20, 50, 500);
        original.setForestId("forest-1");
        original.setX(2);
        original.setY(3);

        when(plantRepository.findById("p1")).thenReturn(Optional.of(original));
        when(plantRepository.save(any(Plant.class))).thenAnswer(inv -> inv.getArgument(0));

        Plant clone = stimulusService.clonePlantToForest("p1", "target", 7, 8);

        assertNotNull(clone);
        assertTrue(clone.getName().contains("Clone"));
        assertNull(clone.getForestId());
        assertNull(clone.getX());
        assertNull(clone.getY());
    }
}
