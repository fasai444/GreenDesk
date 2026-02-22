package org.example.entites.plant;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PlantEffectTest {

    @Test
    void testDefaultConstructor() {
        PlantEffect effect = new PlantEffect();
        assertNull(effect.getId());
        assertNull(effect.getPlantId());
        assertNull(effect.getEffectId());
        assertNull(effect.getStartAt());
        assertNull(effect.getEndAt());
        assertFalse(effect.isActive());
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 12, 0);
        int duration = 5;
        
        PlantEffect effect = new PlantEffect("plant-1", "effect-1", start, duration);
        
        assertEquals("plant-1", effect.getPlantId());
        assertEquals("effect-1", effect.getEffectId());
        assertEquals(start, effect.getStartAt());
        assertEquals(start.plusHours(5), effect.getEndAt());
        assertTrue(effect.isActive());
        assertNull(effect.getId());
    }

    @Test
    void testIsActiveAt() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 12, 0);
        PlantEffect effect = new PlantEffect("plant-1", "effect-1", start, 2); // active from 12:00 to 14:00
        
        // Before start
        assertFalse(effect.isActiveAt(LocalDateTime.of(2023, 1, 1, 11, 0)));
        // Exact start (isAfter is strictly greater)
        assertFalse(effect.isActiveAt(start)); 
        // During effect
        assertTrue(effect.isActiveAt(LocalDateTime.of(2023, 1, 1, 13, 0)));
        // Exact end (isBefore is strictly less)
        assertFalse(effect.isActiveAt(LocalDateTime.of(2023, 1, 1, 14, 0)));
        // After end
        assertFalse(effect.isActiveAt(LocalDateTime.of(2023, 1, 1, 15, 0)));
        
        // If explicitly set to inactive
        effect.setActive(false);
        assertFalse(effect.isActiveAt(LocalDateTime.of(2023, 1, 1, 13, 0)));
    }

    @Test
    void testCheckAndUpdateStatus() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 12, 0);
        PlantEffect effect = new PlantEffect("plant-1", "effect-1", start, 2); // ends at 14:00
        
        assertTrue(effect.isActive());
        
        // Check before end time
        effect.checkAndUpdateStatus(LocalDateTime.of(2023, 1, 1, 13, 0));
        assertTrue(effect.isActive());
        
        // Check at exactly end time (isAfter is strict)
        effect.checkAndUpdateStatus(LocalDateTime.of(2023, 1, 1, 14, 0));
        assertTrue(effect.isActive());
        
        // Check after end time
        effect.checkAndUpdateStatus(LocalDateTime.of(2023, 1, 1, 14, 1));
        assertFalse(effect.isActive());
    }
    
    @Test
    void testSetters() {
        PlantEffect effect = new PlantEffect();
        LocalDateTime time = LocalDateTime.now();
        
        effect.setPlantId("p2");
        effect.setEffectId("e2");
        effect.setStartAt(time);
        effect.setEndAt(time.plusHours(1));
        effect.setActive(true);
        
        assertEquals("p2", effect.getPlantId());
        assertEquals("e2", effect.getEffectId());
        assertEquals(time, effect.getStartAt());
        assertEquals(time.plusHours(1), effect.getEndAt());
        assertTrue(effect.isActive());
    }
}