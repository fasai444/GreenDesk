package org.example.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterventionTest {

    @Test
    void testConstructorAndGettersWater() {
        // Étant donné (Given)
        Intervention.InterventionType type = Intervention.InterventionType.WATER;
        double value = 15.5;

        // Quand (When)
        Intervention intervention = new Intervention(type, value);

        // Alors (Then)
        assertEquals(Intervention.InterventionType.WATER, intervention.getType(), "Le type doit être WATER");
        assertEquals(15.5, intervention.getValue(), "La valeur doit être 15.5");
    }

    @Test
    void testConstructorAndGettersPrune() {
        Intervention intervention = new Intervention(Intervention.InterventionType.PRUNE, 2.0);
        
        assertEquals(Intervention.InterventionType.PRUNE, intervention.getType());
        assertEquals(2.0, intervention.getValue());
    }

    @Test
    void testConstructorAndGettersShading() {
        Intervention intervention = new Intervention(Intervention.InterventionType.SHADING, 50.0);
        
        assertEquals(Intervention.InterventionType.SHADING, intervention.getType());
        assertEquals(50.0, intervention.getValue());
    }

    @Test
    void testEnumValuesExist() {
        // Vérifier que l'énumération contient bien les 3 valeurs attendues
        Intervention.InterventionType[] values = Intervention.InterventionType.values();
        assertEquals(3, values.length, "Il doit y avoir exactement 3 types d'intervention");
        
        // Vérifier la présence exacte
        assertDoesNotThrow(() -> Intervention.InterventionType.valueOf("WATER"));
        assertDoesNotThrow(() -> Intervention.InterventionType.valueOf("PRUNE"));
        assertDoesNotThrow(() -> Intervention.InterventionType.valueOf("SHADING"));
    }
}