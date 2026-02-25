package org.example.entities.ecosystem;

import org.example.entities.forest.Forest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class EcosystemTest {

    private Forest mockForest;
    private Ecosystem ecosystem;

    @BeforeEach
    void setUp() {
        mockForest = Mockito.mock(Forest.class);
        ecosystem = new Ecosystem(mockForest);
    }

    @Test
    void testInitialiseEcosystem() {
        // Création de fausses cellules de forêt
        Forest.ForestCell cell1 = Mockito.mock(Forest.ForestCell.class);
        Forest.ForestCell cell2 = Mockito.mock(Forest.ForestCell.class);
        when(mockForest.getCells()).thenReturn(Arrays.asList(cell1, cell2));

        ecosystem.initialiseEcosystem();

        assertNotNull(ecosystem.getCells());
        assertEquals(2, ecosystem.getCells().size(), "L'écosystème doit contenir exactement 2 cellules.");
        assertEquals(cell1, ecosystem.getCells().get(0).getForestCell());
        assertEquals(cell2, ecosystem.getCells().get(1).getForestCell());
    }
}