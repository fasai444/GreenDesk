package org.example.repositories;

import org.example.entities.weather.WeatherAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherAlertRepository extends MongoRepository<WeatherAlert, String> {
    
    Optional<WeatherAlert> findByEventId(String eventId);
    
    List<WeatherAlert> findByTypeAndTimestampBetween(String type, LocalDateTime start, LocalDateTime end);
    
    List<WeatherAlert> findByProcessedFalse();
}