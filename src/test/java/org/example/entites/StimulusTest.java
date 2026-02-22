package org.example.entites;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class StimulusTest {

    @Test
    void testNoArgsConstructorInitializesCreatedAt() {
        Stimulus stimulus = new Stimulus();
        
        assertNotNull(stimulus.getCreatedAt(), "Le constructeur vide doit initialiser createdAt");
        
        // Vérifie que la date créée est cohérente (par exemple, créée dans la dernière seconde)
        assertTrue(stimulus.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(stimulus.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testParameterizedConstructor() {
        Stimulus stimulus = new Stimulus("HEATWAVE", "forest-XYZ", 15.0, 48);

        assertEquals("HEATWAVE", stimulus.getType());
        assertEquals("forest-XYZ", stimulus.getForestId());
        assertEquals(15.0, stimulus.getIntensity());
        assertEquals(48, stimulus.getDurationHours());
        assertNull(stimulus.getId(), "L'ID doit être null avant persistance en base");
        
        assertNotNull(stimulus.getCreatedAt(), "Le constructeur paramétré doit initialiser createdAt");
    }

    @Test
    void testSettersAndGetters() {
        Stimulus stimulus = new Stimulus();
        LocalDateTime customTime = LocalDateTime.of(2025, 5, 20, 10, 0);

        stimulus.setId("stimulus-1");
        stimulus.setType("STORM");
        stimulus.setForestId("forest-ABC");
        stimulus.setIntensity(120.5); // ex: vent à 120.5 km/h
        stimulus.setDurationHours(5);
        stimulus.setCreatedAt(customTime);

        assertEquals("stimulus-1", stimulus.getId());
        assertEquals("STORM", stimulus.getType());
        assertEquals("forest-ABC", stimulus.getForestId());
        assertEquals(120.5, stimulus.getIntensity());
        assertEquals(5, stimulus.getDurationHours());
        assertEquals(customTime, stimulus.getCreatedAt());
    }
    
    @Test
    void testChangeStateModifiesNothingElse() {
        // S'assurer que modifier une valeur n'impacte pas les autres
        Stimulus stimulus = new Stimulus("RAIN", "forest-1", 10.0, 2);
        LocalDateTime originalTime = stimulus.getCreatedAt();
        
        stimulus.setIntensity(25.0);
        
        assertEquals(25.0, stimulus.getIntensity());
        assertEquals("RAIN", stimulus.getType());
        assertEquals("forest-1", stimulus.getForestId());
        assertEquals(originalTime, stimulus.getCreatedAt(), "La date de création ne doit pas changer lors d'une mise à jour de l'intensité");
    }
}