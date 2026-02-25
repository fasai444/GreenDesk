package org.example.services;

import org.example.entities.effect.Effect;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantEffect;
import org.example.entities.species.Species;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EffectServiceTest {

    @Mock
    private EffectRepository effectRepository;

    @Mock
    private PlantEffectRepository plantEffectRepository;

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private EffectService effectService;

    private Effect mockEffect;
    private PlantEffect mockPlantEffect;

    @BeforeEach
    void setUp() {
        mockEffect = new Effect("Watering", "Adds water", 5);
        mockEffect.setWaterModifier(10.0);

        mockPlantEffect = new PlantEffect("plant-1", "effect-1", LocalDateTime.now(), 5);
    }

    @Test
    void testInitializeEffectsCatalog() {
        when(effectRepository.count()).thenReturn(0L);

        effectService.initializeEffectsCatalog();

        verify(effectRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateCustomEffect() {
        Effect inputEffect = new Effect("Custom", "Desc", 2);
        when(effectRepository.save(inputEffect)).thenReturn(inputEffect);

        Effect saved = effectService.createCustomEffect(inputEffect);

        assertTrue(saved.getIsCustom());
        verify(effectRepository, times(1)).save(inputEffect);
    }

    @Test
    void testApplyEffectToPlant_Success() throws Exception {
        when(plantRepository.existsById("plant-1")).thenReturn(true);
        when(effectRepository.findById("effect-1")).thenReturn(Optional.of(mockEffect));
        when(plantEffectRepository.save(any(PlantEffect.class))).thenReturn(mockPlantEffect);

        PlantEffect result = effectService.applyEffectToPlant("plant-1", "effect-1");

        assertNotNull(result);
        verify(plantEffectRepository, times(1)).save(any(PlantEffect.class));
    }

    @Test
    void testApplyEffectToPlant_AppliesImmediateImpact() throws Exception {
        Species species = new Species("DemoSpecies", 400.0, 20.0, 60.0, 2500.0, 2.0, 0.5);
        Plant plant = new Plant("DemoPlant", species, 400.0, 20.0, 60.0, 2500.0);
        plant.evaluateState();
        double stressBefore = plant.getStressIndex();

        Effect heat = new Effect("HEAT", "Heat effect", 3);
        heat.setTemperatureModifier(5.0);
        heat.setWaterModifier(-20.0);
        heat.setStressReduction(-0.1);

        when(plantRepository.existsById("plant-1")).thenReturn(true);
        when(effectRepository.findById("effect-1")).thenReturn(Optional.of(heat));
        when(plantEffectRepository.save(any(PlantEffect.class))).thenReturn(mockPlantEffect);
        when(plantRepository.findById("plant-1")).thenReturn(Optional.of(plant));

        effectService.applyEffectToPlant("plant-1", "effect-1");

        assertEquals(25.0, plant.getTemperature());
        assertEquals(380.0, plant.getWaterLevel());
        assertTrue(plant.getStressIndex() > stressBefore);
        verify(plantRepository, atLeastOnce()).save(plant);
    }

    @Test
    void testApplyEffectToPlant_PlantNotFound() {
        when(plantRepository.existsById("plant-1")).thenReturn(false);

        Exception exception = assertThrows(Exception.class, () -> {
            effectService.applyEffectToPlant("plant-1", "effect-1");
        });
        assertTrue(exception.getMessage().contains("Plante introuvable"));
    }

    @Test
    void testGetActivePlantEffects() {
        PlantEffect activeEffect = new PlantEffect("p1", "e1", LocalDateTime.now(), 5);
        PlantEffect expiredEffect = new PlantEffect("p1", "e2", LocalDateTime.now().minusHours(10), 5); // Started 10h
                                                                                                        // ago, duration
                                                                                                        // 5h

        when(plantEffectRepository.findByPlantIdAndActive("p1", true))
                .thenReturn(Arrays.asList(activeEffect, expiredEffect));

        List<PlantEffect> activeEffects = effectService.getActivePlantEffects("p1");

        assertEquals(1, activeEffects.size());
        assertEquals(activeEffect, activeEffects.get(0));
        verify(plantEffectRepository, times(1)).save(expiredEffect); // Verify expired effect was saved as inactive
    }

    @Test
    void testRemoveEffectFromPlant() throws Exception {
        when(plantEffectRepository.findById("pe-1")).thenReturn(Optional.of(mockPlantEffect));

        effectService.removeEffectFromPlant("pe-1");

        assertFalse(mockPlantEffect.isActive());
        verify(plantEffectRepository, times(1)).save(mockPlantEffect);
    }

    @Test
    void testCalculateTotalModifiers() {
        PlantEffect active1 = new PlantEffect("p1", "e1", LocalDateTime.now(), 5);
        PlantEffect active2 = new PlantEffect("p1", "e2", LocalDateTime.now(), 5);

        when(plantEffectRepository.findByPlantIdAndActive("p1", true)).thenReturn(Arrays.asList(active1, active2));

        Effect effect1 = new Effect("E1", "", 5);
        effect1.setTemperatureModifier(5.0);
        effect1.setWaterModifier(10.0);
        Effect effect2 = new Effect("E2", "", 5);
        effect2.setTemperatureModifier(2.0);
        effect2.setLuxModifier(100.0);

        when(effectRepository.findById("e1")).thenReturn(Optional.of(effect1));
        when(effectRepository.findById("e2")).thenReturn(Optional.of(effect2));

        EffectService.EffectModifiers modifiers = effectService.calculateTotalModifiers("p1");

        assertEquals(7.0, modifiers.temperature); // 5.0 + 2.0
        assertEquals(10.0, modifiers.water);
        assertEquals(100.0, modifiers.lux);
        assertEquals(0.0, modifiers.humidity);
    }
}