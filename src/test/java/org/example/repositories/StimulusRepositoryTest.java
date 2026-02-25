package org.example.repositories;

import org.example.entities.Stimulus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
class StimulusRepositoryTest {

    @Autowired
    private StimulusRepository stimulusRepository;

    @BeforeEach
    void setUp() {
        Stimulus heatwave = new Stimulus("HEATWAVE", "forest-1", 10.0, 48);
        Stimulus storm = new Stimulus("STORM", "forest-1", 100.0, 12);
        Stimulus rain = new Stimulus("RAIN", "forest-2", 50.0, 24);

        stimulusRepository.saveAll(List.of(heatwave, storm, rain));
    }

    @AfterEach
    void tearDown() {
        stimulusRepository.deleteAll();
    }

    @Test
    void testFindByForestId() {
        List<Stimulus> forest1Stimuli = stimulusRepository.findByForestId("forest-1");
        
        assertEquals(2, forest1Stimuli.size(), "Il devrait y avoir 2 stimulus pour la forêt 1");
        assertTrue(forest1Stimuli.stream().anyMatch(s -> s.getType().equals("HEATWAVE")));
        assertTrue(forest1Stimuli.stream().anyMatch(s -> s.getType().equals("STORM")));

        List<Stimulus> forest2Stimuli = stimulusRepository.findByForestId("forest-2");
        assertEquals(1, forest2Stimuli.size());
        assertEquals("RAIN", forest2Stimuli.get(0).getType());
        
        List<Stimulus> unknownForest = stimulusRepository.findByForestId("unknown");
        assertTrue(unknownForest.isEmpty());
    }
}