package org.example.repositories;

import org.example.entites.alert.AlertType;
import org.example.entites.alert.PlantAlert;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlantAlertRepository extends MongoRepository<PlantAlert, String> {

    List<PlantAlert> findByPlantIdOrderByCreatedAtDesc(String plantId);

    List<PlantAlert> findByPlantIdAndAcknowledgedFalseOrderByCreatedAtDesc(String plantId);

    Optional<PlantAlert> findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(String plantId, AlertType type);

    List<PlantAlert> findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime since);
}