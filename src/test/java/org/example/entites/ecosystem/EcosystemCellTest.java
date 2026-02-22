package org.example.entites.ecosystem;

import org.example.entites.ecosystem.diseases.PlantDisease;
import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EcosystemCellTest {

    private Forest.ForestCell mockForestCell;
    private EcosystemCell cell;

    @BeforeEach
    void setUp() {
        mockForestCell = mock(Forest.ForestCell.class);
        cell = new EcosystemCell(mockForestCell);
    }

    @Test
    void testHasPlant() {
        when(mockForestCell.getPlantId()).thenReturn(null);
        assertFalse(cell.hasPlant(), "La cellule ne doit pas avoir de plante si l'ID est null");

        when(mockForestCell.getPlantId()).thenReturn("plant-123");
        assertTrue(cell.hasPlant(), "La cellule doit avoir une plante si l'ID est présent");
    }

    @Test
    void testInfectAndRecover() {
        PlantDisease mockDisease = mock(PlantDisease.class);
        
        // Impossible d'infecter s'il n'y a pas de plante
        when(mockForestCell.getPlantId()).thenReturn(null);
        cell.infect(mockDisease);
        assertFalse(cell.isDiseased());

        // Infection réussie
        when(mockForestCell.getPlantId()).thenReturn("plant-123");
        cell.infect(mockDisease);
        assertTrue(cell.isDiseased());
        assertEquals(mockDisease, cell.getDisease());

        // Guérison
        cell.recover();
        assertFalse(cell.isDiseased());
        assertNull(cell.getDisease());
    }

    @Test
    void testProgressDisease() {
        PlantDisease mockDisease = mock(PlantDisease.class);
        Plant mockPlant = mock(Plant.class);

        when(mockForestCell.getPlantId()).thenReturn("plant-1");
        cell.infect(mockDisease);
        cell.progressDisease(mockPlant);

        verify(mockDisease, times(1)).progress(mockPlant);
    }

    @Test
    void testGetNeighbors() {
        Ecosystem mockEcosystem = mock(Ecosystem.class);

        // Configuration d'une grille 3x3 avec notre cellule au centre (1,1)
        when(mockForestCell.getX()).thenReturn(1);
        when(mockForestCell.getY()).thenReturn(1);

        EcosystemCell neighbor1 = createMockCell(0, 0);
        EcosystemCell neighbor2 = createMockCell(1, 2);
        EcosystemCell farCell = createMockCell(3, 3); // Trop loin

        when(mockEcosystem.getCells()).thenReturn(Arrays.asList(cell, neighbor1, neighbor2, farCell));

        List<EcosystemCell> neighbors = cell.getNeighbors(mockEcosystem);

        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(neighbor1));
        assertTrue(neighbors.contains(neighbor2));
        assertFalse(neighbors.contains(farCell));
        assertFalse(neighbors.contains(cell)); // Ne doit pas se contenir elle-même
    }

    @Test
    void testGetMostSevereNeighborDisease() {
        Ecosystem mockEcosystem = mock(Ecosystem.class);
        when(mockForestCell.getX()).thenReturn(1);
        when(mockForestCell.getY()).thenReturn(1);

        // Voisin 1 : Maladie A (sévérité 0.4)
        EcosystemCell n1 = createMockCell(0, 1, true, "DiseaseA", 0.4);
        // Voisin 2 : Maladie A (sévérité 0.8) -> Moyenne DiseaseA = 0.6
        EcosystemCell n2 = createMockCell(1, 0, true, "DiseaseA", 0.8);
        // Voisin 3 : Maladie B (sévérité 0.7) -> Moyenne DiseaseB = 0.7
        EcosystemCell n3 = createMockCell(2, 1, true, "DiseaseB", 0.7);

        when(mockEcosystem.getCells()).thenReturn(Arrays.asList(cell, n1, n2, n3));

        PlantDisease mostSevere = cell.getMostSevereNeighborDisease(mockEcosystem);

        assertNotNull(mostSevere);
        // Le mock est configuré pour que copy() renvoie le même mock
        assertEquals("DiseaseB", mostSevere.getName()); 
    }

    @Test
    void testShouldBecomeInfected() {
        Ecosystem mockEcosystem = mock(Ecosystem.class);
        when(mockForestCell.getPlantId()).thenReturn("my-plant");
        when(mockForestCell.getX()).thenReturn(1);
        when(mockForestCell.getY()).thenReturn(1);

        // 2 voisins avec plante : 1 malade, 1 sain (ratio = 0.5)
        EcosystemCell n1 = createMockCell(0, 1, true, "Any", 0.5); // malade
        EcosystemCell n2 = createMockCell(1, 0, false, null, 0);   // sain avec plante (isDiseased = false)
        when(n2.hasPlant()).thenReturn(true);

        when(mockEcosystem.getCells()).thenReturn(Arrays.asList(cell, n1, n2));

        assertTrue(cell.shouldBecomeInfected(mockEcosystem, 0.5), "Ratio 0.5 >= seuil 0.5");
        assertFalse(cell.shouldBecomeInfected(mockEcosystem, 0.6), "Ratio 0.5 < seuil 0.6");
    }

    // --- Helper methods ---
    private EcosystemCell createMockCell(int x, int y) {
        Forest.ForestCell fc = mock(Forest.ForestCell.class);
        when(fc.getX()).thenReturn(x);
        when(fc.getY()).thenReturn(y);
        return new EcosystemCell(fc);
    }

    private EcosystemCell createMockCell(int x, int y, boolean isDiseased, String diseaseName, double severity) {
        EcosystemCell mockCell = mock(EcosystemCell.class);
        Forest.ForestCell fc = mock(Forest.ForestCell.class);
        when(fc.getX()).thenReturn(x);
        when(fc.getY()).thenReturn(y);
        when(mockCell.getForestCell()).thenReturn(fc);
        when(mockCell.hasPlant()).thenReturn(true);
        when(mockCell.isDiseased()).thenReturn(isDiseased);

        if (isDiseased) {
            PlantDisease d = mock(PlantDisease.class);
            when(d.getName()).thenReturn(diseaseName);
            when(d.getSeverity()).thenReturn(severity);
            when(d.copy()).thenReturn(d);
            when(mockCell.getDisease()).thenReturn(d);
        }
        return mockCell;
    }
}