package org.example.repositories;

import org.example.entities.SensorReading;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'historique des lectures capteurs
 * Inspiré du projet GreenDesk, adapté à MongoDB et à notre simulation
 */
@Repository
public interface SensorReadingRepository extends MongoRepository<SensorReading, String> {

    /**
     * Dernières lectures pour une plante (triées par date descendante)
     */
    List<SensorReading> findByPlantIdOrderByTimestampDesc(String plantId);

    /**
     * Lectures dans une plage de dates pour une plante
     */
    List<SensorReading> findByPlantIdAndTimestampBetweenOrderByTimestampDesc(
        String plantId, LocalDateTime start, LocalDateTime end);

    /**
     * Lecture la plus récente pour une plante
     */
    Optional<SensorReading> findFirstByPlantIdOrderByTimestampDesc(String plantId);

    /**
     * Lectures avec humidité trop basse (alerte déshydratation)
     */
    List<SensorReading> findByHumidityLessThan(double threshold);

    /**
     * Lectures avec température trop haute
     */
    List<SensorReading> findByTemperatureGreaterThan(double threshold);

    /**
     * Lectures avec lumière insuffisante
     */
    List<SensorReading> findByLuxLessThan(double threshold);

    /**
     * Toutes les lectures récentes (dernières 24h par exemple)
     */
    List<SensorReading> findByTimestampGreaterThanEqualOrderByTimestampDesc(LocalDateTime since);

    /**
     * Lectures pour toutes les plantes, triées par date
     */
    List<SensorReading> findAllByOrderByTimestampDesc();
}