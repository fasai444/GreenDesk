package org.example.entites.plant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlantStateTest {

    @Test
    void testEnumValues() {
        PlantState[] states = PlantState.values();
        assertEquals(4, states.length);
        
        // Verify values exist
        assertDoesNotThrow(() -> PlantState.valueOf("HEALTHY"));
        assertDoesNotThrow(() -> PlantState.valueOf("STRESSED"));
        assertDoesNotThrow(() -> PlantState.valueOf("DORMANT"));
        assertDoesNotThrow(() -> PlantState.valueOf("DISEASED"));
    }
}