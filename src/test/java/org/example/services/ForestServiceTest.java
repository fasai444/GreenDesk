package org.example.services;

import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;
import org.example.entites.species.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForestServiceTest {

    @Mock
    private ForestRepository forestRepository;

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private ForestService forestService;

    private Forest mockForest;
    private Plant mockPlant;
    private Species mockSpecies;

    @BeforeEach
    void setUp() {
        mockForest = new Forest("Sherwood", 10, 10);
        ReflectionTestUtils.setField(mockForest, "id", "f1");

        mockSpecies = new Species();
        mockSpecies.setId("s1");
        mockSpecies.setName("Oak");

        mockPlant = new Plant();
        mockPlant.setName("Tree1");
        mockPlant.setSpecies(mockSpecies);
        mockPlant.setVariationSeed(12345);
        mockPlant.setWaterLevel(50.0);
        mockPlant.setTemperature(20.0);
        mockPlant.setHumidity(60.0);
        mockPlant.setLux(1000.0);
    }

    @Test
    void testCreateForest() {
        when(forestRepository.save(any(Forest.class))).thenReturn(mockForest);

        Forest created = forestService.createForest("Sherwood", 10, 10);

        assertNotNull(created);
        assertEquals("Sherwood", created.getName());
    }

    @Test
    void testAddPlantToForest_Success() throws Exception {
        when(forestRepository.findById("f1")).thenReturn(Optional.of(mockForest));
        when(plantRepository.findById("p1")).thenReturn(Optional.of(mockPlant));
        when(plantRepository.findAllById(anyList())).thenReturn(new ArrayList<>());
        when(plantRepository.save(any(Plant.class))).thenReturn(mockPlant);
        when(forestRepository.save(any(Forest.class))).thenReturn(mockForest);

        Forest result = forestService.addPlantToForest("f1", "p1", 5, 5);

        assertNotNull(result);
        assertTrue(mockForest.isPositionOccupied(5, 5));
        assertEquals("f1", mockPlant.getForestId());
        assertEquals(5, mockPlant.getX());

        verify(plantRepository, times(1)).save(mockPlant);
        verify(forestRepository, times(1)).save(mockForest);
    }

    @Test
    void testAddPlantToForest_R1_PositionOccupied() {
        mockForest.addCell(new Forest.ForestCell(5, 5, "existing-plant"));

        when(forestRepository.findById("f1")).thenReturn(Optional.of(mockForest));
        when(plantRepository.findById("p1")).thenReturn(Optional.of(mockPlant));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                forestService.addPlantToForest("f1", "p1", 5, 5)
        );

        assertTrue(exception.getMessage().contains("déjà occupée"));
    }

    @Test
    void testAddPlantToForest_OutOfBounds() {
        when(forestRepository.findById("f1")).thenReturn(Optional.of(mockForest));
        when(plantRepository.findById("p1")).thenReturn(Optional.of(mockPlant));
        when(plantRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () ->
                forestService.addPlantToForest("f1", "p1", 15, 15)
        );
    }

    @Test
    void testRemovePlantFromForest_Success() throws Exception {
        mockForest.addCell(new Forest.ForestCell(2, 2, "p1"));

        when(forestRepository.findById("f1")).thenReturn(Optional.of(mockForest));
        when(plantRepository.findById("p1")).thenReturn(Optional.of(mockPlant));
        when(forestRepository.save(mockForest)).thenReturn(mockForest);

        Forest result = forestService.removePlantFromForest("f1", 2, 2);

        assertFalse(result.isPositionOccupied(2, 2));
        assertNull(mockPlant.getForestId());
        assertNull(mockPlant.getX());
        verify(plantRepository, times(1)).save(mockPlant);
    }

    @Test
    void testDeleteForest() throws Exception {
        mockForest.addCell(new Forest.ForestCell(1, 1, "p1"));

        when(forestRepository.findById("f1")).thenReturn(Optional.of(mockForest));
        when(plantRepository.findAllById(anyList())).thenReturn(Arrays.asList(mockPlant));

        forestService.deleteForest("f1");

        assertNull(mockPlant.getForestId());
        verify(plantRepository, times(1)).save(mockPlant);
        verify(forestRepository, times(1)).delete(mockForest);
    }
}
