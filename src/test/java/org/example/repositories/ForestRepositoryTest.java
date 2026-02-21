package org.example.repositories;

import org.example.entites.forest.Forest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class ForestRepositoryTest {

    @Autowired
    private ForestRepository forestRepository;

    @BeforeEach
    void setUp() {
        Forest forest = new Forest("Sherwood", 100, 100);
        forestRepository.save(forest);
    }

    @AfterEach
    void tearDown() {
        forestRepository.deleteAll();
    }

    @Test
    void testFindByName() {
        Optional<Forest> found = forestRepository.findByName("Sherwood");
        assertTrue(found.isPresent());
        assertEquals(100, found.get().getWidth());

        Optional<Forest> notFound = forestRepository.findByName("Brocéliande");
        assertFalse(notFound.isPresent());
    }
}