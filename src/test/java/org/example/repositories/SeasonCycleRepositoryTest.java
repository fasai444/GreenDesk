package org.example.repositories;

import org.example.entites.environment.SeasonCycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("SeasonCycleRepository")
class SeasonCycleRepositoryTest {

    @Autowired
    private SeasonCycleRepository repo;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
    }

    @Test
    void findByForestId_not_existing_returns_empty() {
        assertThat(repo.findByForestId("unknown")).isEmpty();
    }

    @Test
    void save_multiple_cycles_different_forestIds() {
        repo.save(new SeasonCycle("f-A"));
        repo.save(new SeasonCycle("f-B"));

        assertThat(repo.findByForestId("f-A")).isPresent();
        assertThat(repo.findByForestId("f-B")).isPresent();
    }
}
