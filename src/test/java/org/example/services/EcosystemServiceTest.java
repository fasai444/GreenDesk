package org.example.services;

import org.example.entities.ecosystem.Ecosystem;
import org.example.entities.ecosystem.EcosystemCell;
import org.example.entities.ecosystem.diseases.PlantDisease;
import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.repositories.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcosystemServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private Ecosystem mockEcosystem;

    @InjectMocks
    private EcosystemService ecosystemService;

    private Plant mockPlant1;
    private PlantDisease mockDisease;

    @BeforeEach
    void setUp() {
        ecosystemService.setEcosystem(mockEcosystem);

        mockPlant1 = new Plant();
        mockPlant1.setName("Tree1");

        mockDisease = mock(PlantDisease.class);
    }

    @Test
    void testTick_NoEcosystemThrowsException() {
        ecosystemService.setEcosystem(null);
        assertThrows(IllegalStateException.class, ecosystemService::tick);
    }

    @Test
    void testTick_DiseaseProgressionAndRecovery() {
        EcosystemCell mockCell1 = mock(EcosystemCell.class);
        when(mockCell1.getForestCell()).thenReturn(new Forest.ForestCell(0, 0, "plant-1"));
        when(mockEcosystem.getCells()).thenReturn(List.of(mockCell1));
        when(mockCell1.hasPlant()).thenReturn(true);
        when(plantRepository.findById("plant-1")).thenReturn(Optional.of(mockPlant1));

        when(mockCell1.isDiseased()).thenReturn(true);
        when(mockCell1.getDisease()).thenReturn(mockDisease);
        when(mockDisease.getRecoveryThreshold()).thenReturn(0.8);
        when(mockCell1.shouldRecover(mockEcosystem, 0.8)).thenReturn(true);

        ecosystemService.tick();

        verify(mockCell1, times(1)).progressDisease(mockPlant1);
        verify(mockCell1, times(1)).recover();
    }

    @Test
    void testTick_DiseaseInfection() {
        EcosystemCell mockCell1 = mock(EcosystemCell.class);
        when(mockCell1.getForestCell()).thenReturn(new Forest.ForestCell(0, 0, "plant-1"));
        when(mockEcosystem.getCells()).thenReturn(List.of(mockCell1));
        when(mockCell1.hasPlant()).thenReturn(true);
        when(plantRepository.findById("plant-1")).thenReturn(Optional.of(mockPlant1));

        when(mockCell1.isDiseased()).thenReturn(false);
        when(mockCell1.getMostSevereNeighborDisease(mockEcosystem)).thenReturn(mockDisease);
        when(mockDisease.getInfectionThreshold()).thenReturn(0.5);
        when(mockDisease.copy()).thenReturn(mockDisease);
        when(mockCell1.shouldBecomeInfected(mockEcosystem, 0.5)).thenReturn(true);

        ecosystemService.tick();

        verify(mockCell1, times(1)).infect(mockDisease);
    }

    @Test
    void testGetEcosystemStatus() {
        EcosystemCell mockCell1 = mock(EcosystemCell.class);
        EcosystemCell mockCell2 = mock(EcosystemCell.class);

        when(mockCell1.getForestCell()).thenReturn(new Forest.ForestCell(0, 0, "plant-1"));
        when(mockCell2.getForestCell()).thenReturn(new Forest.ForestCell(0, 1, "plant-2"));
        when(mockEcosystem.getCells()).thenReturn(List.of(mockCell1, mockCell2));

        when(mockCell1.hasPlant()).thenReturn(true);
        when(mockCell1.isDiseased()).thenReturn(false);

        when(mockCell2.hasPlant()).thenReturn(true);
        when(mockCell2.isDiseased()).thenReturn(true);
        when(mockCell2.getDisease()).thenReturn(mockDisease);
        when(mockDisease.getName()).thenReturn("Rust");
        when(mockDisease.getSeverity()).thenReturn(0.75);

        List<String> status = ecosystemService.getEcosystemStatus();

        assertEquals(2, status.size());
        assertTrue(status.stream().anyMatch(s -> s.contains("healthy")));
        assertTrue(status.stream().anyMatch(s -> s.contains("Rust(")));
    }
}
