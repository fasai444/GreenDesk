package org.example.repositories;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = "spring.data.mongodb.database=greendesk_plant_repository_test")
class PlantRepositoryTest {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    private Plant p1, p2, p3;
    private Species testSpecies;

    @BeforeEach
    void setUp() {
        plantRepository.deleteAll();
        speciesRepository.deleteAll();

        testSpecies = new Species();
        testSpecies.setName("Fougère");
        testSpecies.setOptimalWaterNeeds(70.0);
        testSpecies.setOptimalTemperature(20.0);
        testSpecies.setOptimalHumidity(65.0);
        testSpecies.setOptimalLuxNeeds(1200.0);
        testSpecies.setBaseGrowthRate(1.0);
        testSpecies = speciesRepository.save(testSpecies);

        p1 = new Plant("Tomato_Plant_1", testSpecies);
        p1.setPlantState(PlantState.HEALTHY);
        p1.setStressIndex(0.1);
        p1.setWaterLevel(100.0);
        p1.setForestId("forest-A");

        p2 = new Plant("Oak_Tree", testSpecies);
        p2.setPlantState(PlantState.STRESSED);
        p2.setStressIndex(0.5);
        p2.setWaterLevel(40.0);
        p2.setForestId("forest-A");

        p3 = new Plant("Tomato_Plant_2", testSpecies);
        p3.setPlantState(PlantState.DISEASED);
        p3.setStressIndex(0.9);
        p3.setWaterLevel(10.0);
        p3.setForestId("forest-B");

        List<Plant> saved = plantRepository.saveAll(List.of(p1, p2, p3));
        p1 = saved.get(0);
        p2 = saved.get(1);
        p3 = saved.get(2);
    }

    @AfterEach
    void tearDown() {
        plantRepository.deleteAll();
        speciesRepository.deleteAll();
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Plant> tomatoes = plantRepository.findByNameContainingIgnoreCase("tomato");

        assertEquals(2, tomatoes.size());
        assertTrue(tomatoes.stream().anyMatch(p -> p.getName().equals("Tomato_Plant_1")));
        assertTrue(tomatoes.stream().anyMatch(p -> p.getName().equals("Tomato_Plant_2")));
    }

    @Test
    void testFindByPlantState() {
        List<Plant> diseasedPlants = plantRepository.findByPlantState(PlantState.DISEASED);

        assertEquals(1, diseasedPlants.size());
        assertEquals("Tomato_Plant_2", diseasedPlants.get(0).getName());
    }

    @Test
    void testFindByStressIndexGreaterThan() {
        List<Plant> highlyStressed = plantRepository.findByStressIndexGreaterThan(0.4);

        assertEquals(2, highlyStressed.size());
        assertTrue(highlyStressed.stream().anyMatch(p -> p.getName().equals("Oak_Tree")));
        assertTrue(highlyStressed.stream().anyMatch(p -> p.getName().equals("Tomato_Plant_2")));
    }

    @Test
    void testFindByWaterLevelLessThan() {
        List<Plant> thirstyPlants = plantRepository.findByWaterLevelLessThan(50.0);

        assertEquals(2, thirstyPlants.size());
        assertTrue(thirstyPlants.stream().anyMatch(p -> p.getName().equals("Oak_Tree")));
        assertTrue(thirstyPlants.stream().anyMatch(p -> p.getName().equals("Tomato_Plant_2")));
    }

    @Test
    void testFindByForestId() {
        List<Plant> forestAPlants = plantRepository.findByForestId("forest-A");

        assertEquals(2, forestAPlants.size());
        assertTrue(forestAPlants.stream().anyMatch(p -> p.getName().equals("Tomato_Plant_1")));
        assertTrue(forestAPlants.stream().anyMatch(p -> p.getName().equals("Oak_Tree")));

        List<Plant> forestBPlants = plantRepository.findByForestId("forest-B");
        assertEquals(1, forestBPlants.size());
        assertEquals("Tomato_Plant_2", forestBPlants.get(0).getName());
    }

    @Test
    void testFindTop10ByOrderByIdDesc() {
        plantRepository.deleteAll();
        for (int i = 1; i <= 11; i++) {
            Plant p = new Plant();
            p.setName("Plant_" + i);
            p.setSpecies(testSpecies);
            plantRepository.save(p);
        }

        List<Plant> top10 = plantRepository.findTop10ByOrderByIdDesc();

        assertEquals(10, top10.size());
    }

    @Test
    void testFindAllByOrderByStressIndexDesc() {
        List<Plant> sortedPlants = plantRepository.findAllByOrderByStressIndexDesc();

        assertEquals(3, sortedPlants.size());
        assertEquals("Tomato_Plant_2", sortedPlants.get(0).getName());
        assertEquals("Oak_Tree", sortedPlants.get(1).getName());
        assertEquals("Tomato_Plant_1", sortedPlants.get(2).getName());
    }

    @Test
    void testCountByPlantState() {
        long healthyCount = plantRepository.countByPlantState(PlantState.HEALTHY);
        long stressedCount = plantRepository.countByPlantState(PlantState.STRESSED);
        long dormantCount = plantRepository.countByPlantState(PlantState.DORMANT);

        assertEquals(1, healthyCount);
        assertEquals(1, stressedCount);
        assertEquals(0, dormantCount);
    }
}
