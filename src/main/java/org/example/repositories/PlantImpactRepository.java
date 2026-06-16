package org.example.repositories;

import org.example.entities.weather.PlantImpact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;  //
import java.util.List;

@Repository
public interface PlantImpactRepository extends MongoRepository<PlantImpact, String> {
    
    List<PlantImpact> findByPlantIdOrderByTimestampDesc(String plantId);
    
    List<PlantImpact> findByAlertId(String alertId);
    
    List<PlantImpact> findByPlantIdAndTimestampBetween(String plantId, LocalDateTime start, LocalDateTime end);
}