package org.example.repositories;

import org.example.entities.plant.PlantEffect;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantEffectRepository extends MongoRepository<PlantEffect, String> {
    List<PlantEffect> findByPlantId(String plantId);

    List<PlantEffect> findByPlantIdAndActive(String plantId, boolean active);

    long countByActiveTrue();
}
