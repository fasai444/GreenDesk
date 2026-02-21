package org.example.repositories;

import org.example.entites.species.Species;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class SpeciesRepositoryTest {

    @Autowired
    private SpeciesRepository speciesRepository;

    @BeforeEach
    void setUp() {
        speciesRepository.deleteAll();

        Species cactus = new Species();
        cactus.setName("Cactus");
        cactus.setOptimalWaterNeeds(10.0);
        cactus.setOptimalTemperature(30.0);
        cactus.setOptimalLuxNeeds(2000.0);
        cactus.setBaseGrowthRate(0.5);

        Species fern = new Species();
        fern.setName("Fougère");
        fern.setOptimalWaterNeeds(80.0);
        fern.setOptimalTemperature(18.0);
        fern.setOptimalLuxNeeds(500.0);
        fern.setBaseGrowthRate(2.0);

        speciesRepository.saveAll(List.of(cactus, fern));
    }

    @AfterEach
    void tearDown() {
        speciesRepository.deleteAll();
    }

    @Test
    void testFindByName() {
        Optional<Species> found = speciesRepository.findByName("Cactus");
        assertTrue(found.isPresent());
        assertEquals(10.0, found.get().getOptimalWaterNeeds());
    }

    @Test
    void testFindByOptimalWaterNeedsBetween() {
        List<Species> waterLovers = speciesRepository.findByOptimalWaterNeedsBetween(50.0, 100.0);
        assertEquals(1, waterLovers.size());
        assertEquals("Fougère", waterLovers.get(0).getName());
    }

    @Test
    void testFindByOptimalTemperatureLessThanEqual() {
        List<Species> coolSpecies = speciesRepository.findByOptimalTemperatureLessThanEqual(25.0);
        assertEquals(1, coolSpecies.size());
        assertEquals("Fougère", coolSpecies.get(0).getName());
    }

    @Test
    void testFindAllByOrderByBaseGrowthRateDesc() {
        List<Species> sorted = speciesRepository.findAllByOrderByBaseGrowthRateDesc();
        assertEquals(2, sorted.size());
        assertEquals("Fougère", sorted.get(0).getName());
        assertEquals("Cactus", sorted.get(1).getName());
    }
}
