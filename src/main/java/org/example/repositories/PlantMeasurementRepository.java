package org.example.repositories;

import org.example.entities.plant.PlantMeasurement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlantMeasurementRepository extends MongoRepository<PlantMeasurement, String> {
    List<PlantMeasurement> findByPlantIdOrderByTimestampDesc(String plantId);
    List<PlantMeasurement> findByPlantIdAndTimestampBetween(String plantId, LocalDateTime start, LocalDateTime end);
}