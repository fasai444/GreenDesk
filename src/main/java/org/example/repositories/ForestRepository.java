package org.example.repositories;

import org.example.entities.forest.Forest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForestRepository extends MongoRepository<Forest, String> {
    Optional<Forest> findByName(String name);
}
