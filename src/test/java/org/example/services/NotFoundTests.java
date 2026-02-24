package org.example.services;

import org.springframework.boot.test.context.SpringBootTest; 
import org.springframework.beans.factory.annotation.Autowired;

// Importations JUnit 5 (Jupiter)
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;

@SpringBootTest
public class NotFoundTests {

    @Autowired
    private PlantServices plantService;

    @Autowired
    private ForestService forestService;

    @Test
    void testGetPlantById_NotFound() throws Exception {
        // On vérifie que chercher un ID imaginaire renvoie un Optional vide
        Optional<Plant> result = plantService.getPlantById("ID_QUI_N_EXISTE_PAS");
        assertTrue(result.isEmpty(), "La plante ne devrait pas être trouvée");
    }

    @Test
    void testGetForestById_NotFound() {
        Optional<Forest> result = forestService.getForestById("FORET_FANTOME");
        assertTrue(result.isEmpty(), "La forêt ne devrait pas être trouvée");
    }
}
