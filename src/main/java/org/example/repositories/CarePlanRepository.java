package org.example.repositories;

import org.example.entities.care.CarePlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarePlanRepository extends MongoRepository<CarePlan, String> {
    Optional<CarePlan> findByPlantId(String plantId);
}