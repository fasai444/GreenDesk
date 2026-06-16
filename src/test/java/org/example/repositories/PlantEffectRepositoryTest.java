package org.example.repositories;

import org.example.entities.plant.PlantEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class PlantEffectRepositoryTest {

    @Autowired
    private PlantEffectRepository plantEffectRepository;

    @BeforeEach
    void setUp() {
        PlantEffect activeEffect = new PlantEffect("plant-1", "effect-A", LocalDateTime.now(), 5);
        
        PlantEffect inactiveEffect = new PlantEffect("plant-1", "effect-B", LocalDateTime.now().minusDays(1), 2);
        inactiveEffect.setActive(false);

        PlantEffect otherPlantEffect = new PlantEffect("plant-2", "effect-A", LocalDateTime.now(), 5);

        plantEffectRepository.saveAll(List.of(activeEffect, inactiveEffect, otherPlantEffect));
    }

    @AfterEach
    void tearDown() {
        plantEffectRepository.deleteAll();
    }

    @Test
    void testFindByPlantId() {
        List<PlantEffect> effects = plantEffectRepository.findByPlantId("plant-1");
        assertEquals(2, effects.size());
    }

    @Test
    void testFindByPlantIdAndActive() {
        List<PlantEffect> activeEffects = plantEffectRepository.findByPlantIdAndActive("plant-1", true);
        assertEquals(1, activeEffects.size());
        assertEquals("effect-A", activeEffects.get(0).getEffectId());

        List<PlantEffect> inactiveEffects = plantEffectRepository.findByPlantIdAndActive("plant-1", false);
        assertEquals(1, inactiveEffects.size());
        assertEquals("effect-B", inactiveEffects.get(0).getEffectId());
    }
}