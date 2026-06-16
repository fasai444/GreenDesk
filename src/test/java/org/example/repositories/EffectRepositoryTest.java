package org.example.repositories;

import org.example.entities.effect.Effect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class EffectRepositoryTest {

    @Autowired
    private EffectRepository effectRepository;

    private Effect customEffect;
    private Effect presetEffect;

    @BeforeEach
    void setUp() {
        customEffect = new Effect("Magic Fertilizer", "Super strong", 24);
        customEffect.setCustom(true);

        presetEffect = Effect.createShadeEffect(); // isCustom = false by default

        effectRepository.saveAll(List.of(customEffect, presetEffect));
    }

    @AfterEach
    void tearDown() {
        effectRepository.deleteAll();
    }

    @Test
    void testFindByName() {
        Optional<Effect> found = effectRepository.findByName("Magic Fertilizer");
        
        assertTrue(found.isPresent(), "L'effet devrait être trouvé par son nom");
        assertEquals(24, found.get().getDurationHours());

        Optional<Effect> notFound = effectRepository.findByName("Unknown");
        assertFalse(notFound.isPresent());
    }

    @Test
    void testFindByIsCustom() {
        List<Effect> customEffects = effectRepository.findByIsCustom(true);
        assertEquals(1, customEffects.size());
        assertEquals("Magic Fertilizer", customEffects.get(0).getName());

        List<Effect> presetEffects = effectRepository.findByIsCustom(false);
        assertEquals(1, presetEffects.size());
        assertEquals("Shade", presetEffects.get(0).getName());
    }
}