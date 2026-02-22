package org.example.entites.forest;

import org.example.entites.plant.Plant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ForestTest {

    private Forest forest;

    @BeforeEach
    void setUp() {
        forest = new Forest("Test Forest", 5, 5);
    }

    @Test
    void testConstructors() {
        // Default constructor
        Forest defaultForest = new Forest();
        assertNotNull(defaultForest.getCells());
        assertNotNull(defaultForest.getPlants());
        assertNotNull(defaultForest.getCreatedAt());

        // Parameterized constructor
        assertEquals("Test Forest", forest.getName());
        assertEquals(5, forest.getWidth());
        assertEquals(5, forest.getHeight());
        assertNotNull(forest.getCreatedAt());
        assertTrue(forest.getCells().isEmpty());
    }

    @Test
    void testAddCellSuccess() {
        Forest.ForestCell cell = new Forest.ForestCell(2, 2, "plant-1");
        forest.addCell(cell);

        assertEquals(1, forest.getCells().size());
        assertTrue(forest.isPositionOccupied(2, 2));
    }

    @Test
    void testAddCellNullThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            forest.addCell(null);
        });
        assertEquals("Cell cannot be null", exception.getMessage());
    }

    @Test
    void testAddCellOutOfBoundsThrowsException() {
        Forest.ForestCell cell1 = new Forest.ForestCell(-1, 0, "plant-1");
        Forest.ForestCell cell2 = new Forest.ForestCell(0, 5, "plant-2"); // height is 5, max index 4
        
        assertThrows(IllegalArgumentException.class, () -> forest.addCell(cell1));
        assertThrows(IllegalArgumentException.class, () -> forest.addCell(cell2));
    }

    @Test
    void testAddCellPositionOccupiedThrowsException() {
        forest.addCell(new Forest.ForestCell(1, 1, "plant-1"));
        
        Forest.ForestCell duplicateCell = new Forest.ForestCell(1, 1, "plant-2");
        assertThrows(IllegalArgumentException.class, () -> forest.addCell(duplicateCell));
    }

    @Test
    void testIsPositionOccupied() {
        assertFalse(forest.isPositionOccupied(1, 1));
        forest.addCell(new Forest.ForestCell(1, 1, "plant-1"));
        assertTrue(forest.isPositionOccupied(1, 1));
    }

    @Test
    void testGetCellAt() {
        Forest.ForestCell cell = new Forest.ForestCell(3, 4, "plant-1");
        forest.addCell(cell);

        Optional<Forest.ForestCell> foundCell = forest.getCellAt(3, 4);
        assertTrue(foundCell.isPresent());
        assertEquals("plant-1", foundCell.get().getPlantId());

        Optional<Forest.ForestCell> notFound = forest.getCellAt(0, 0);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testRemoveCellByPlantId() {
        forest.addCell(new Forest.ForestCell(0, 0, "plant-1"));
        forest.addCell(new Forest.ForestCell(1, 1, "plant-2"));

        assertTrue(forest.removeCellByPlantId("plant-1"));
        assertEquals(1, forest.getCells().size());
        assertFalse(forest.isPositionOccupied(0, 0));
        
        // Remove non-existent or null
        assertFalse(forest.removeCellByPlantId("non-existent"));
        assertFalse(forest.removeCellByPlantId(null));
    }

    @Test
    void testRemoveCellAt() {
        forest.addCell(new Forest.ForestCell(2, 2, "plant-1"));
        
        assertTrue(forest.removeCellAt(2, 2));
        assertEquals(0, forest.getCells().size());
        
        assertFalse(forest.removeCellAt(0, 0)); // Not there
    }

    @Test
    void testSettersAndGetters() {
        forest.setName("New Name");
        forest.setWidth(10);
        forest.setHeight(10);
        
        assertEquals("New Name", forest.getName());
        assertEquals(10, forest.getWidth());
        assertEquals(10, forest.getHeight());
        assertNull(forest.getId()); // ID is typically set by DB

        List<Forest.ForestCell> cells = new ArrayList<>();
        cells.add(new Forest.ForestCell(0,0,"plant1"));
        forest.setCells(cells);
        assertEquals(1, forest.getCells().size());
        
        // Test null handling in setters
        forest.setCells(null);
        assertNotNull(forest.getCells());
        assertTrue(forest.getCells().isEmpty());
        
        List<Plant> plants = new ArrayList<>();
        plants.add(new Plant());
        forest.setPlants(plants);
        assertEquals(1, forest.getPlants().size());
        
        forest.setPlants(null);
        assertNotNull(forest.getPlants());
        assertTrue(forest.getPlants().isEmpty());
    }

    @Test
    void testForestCellEqualsAndHashCode() {
        Forest.ForestCell cell1 = new Forest.ForestCell(1, 1, "plant-1");
        Forest.ForestCell cell2 = new Forest.ForestCell(1, 1, "plant-1");
        Forest.ForestCell cell3 = new Forest.ForestCell(1, 2, "plant-1");
        
        assertEquals(cell1, cell2);
        assertEquals(cell1.hashCode(), cell2.hashCode());
        assertNotEquals(cell1, cell3);
        assertNotEquals(cell1, null);
        assertNotEquals(cell1, new Object());
    }
}