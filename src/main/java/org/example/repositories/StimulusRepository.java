package org.example.repositories;

import org.example.entites.Stimulus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StimulusRepository extends MongoRepository<Stimulus, String> {
    // Permet de retrouver l'historique des stimulus par forêt si besoin
    java.util.List<Stimulus> findByForestId(String forestId);
}
