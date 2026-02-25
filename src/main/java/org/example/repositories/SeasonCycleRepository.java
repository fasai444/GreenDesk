package org.example.repositories;

import org.example.entities.environment.SeasonCycle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonCycleRepository extends MongoRepository<SeasonCycle, String> {
    Optional<SeasonCycle> findByForestId(String forestId);
}
