package org.example.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.entities.alert.AlertType;
import org.example.entities.alert.PlantAlert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlantAlertRepository extends MongoRepository<PlantAlert, String> {

    List<PlantAlert> findByPlantIdOrderByCreatedAtDesc(String plantId);

    List<PlantAlert> findByPlantIdAndAcknowledgedFalseOrderByCreatedAtDesc(String plantId);

    Optional<PlantAlert> findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(String plantId,
            AlertType type);

    List<PlantAlert> findByAcknowledgedFalseAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime since);

    List<PlantAlert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    long countByAcknowledgedFalse();

    long countByPlantIdInAndAcknowledgedFalse(List<String> plantIds);
}