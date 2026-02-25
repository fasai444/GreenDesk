package org.example.services;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.entities.plant.Plant;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SensorReadingService {

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private PlantAlertService plantAlertService;

    public SensorReading addReading(String plantId, CreateSensorReadingRequest req) throws Exception {
        String safePlantId = Objects.requireNonNull(plantId, "plantId must not be null");
        Plant plant = plantRepository.findById(safePlantId)
                .orElseThrow(() -> new Exception("Plante introuvable: " + safePlantId));

        LocalDateTime ts = req.getTimestamp() != null ? req.getTimestamp() : LocalDateTime.now();

        SensorReading reading = new SensorReading(
                safePlantId,
                ts,
                req.getTemperature(),
                req.getHumidity(),
                req.getLux(),
                req.getRainfall());

        SensorReading saved = sensorReadingRepository.save(reading);

        // Update "current" environment on plant
        plant.setTemperature(req.getTemperature());
        plant.setHumidity(req.getHumidity());
        plant.setLux(req.getLux());

        // Simple mapping rainfall -> waterLevel (adapt if you want a better model)
        plant.setWaterLevel(Math.max(0, plant.getWaterLevel() + req.getRainfall()));

        // Recompute state/stress
        plant.setPlantState(plant.evaluateState());
        plantRepository.save(plant);

        // Trigger alerts
        plantAlertService.evaluateAndCreateAlerts(plant);

        return saved;
    }

    public List<SensorReading> getReadings(String plantId) {
        return sensorReadingRepository.findByPlantIdOrderByTimestampDesc(
                Objects.requireNonNull(plantId, "plantId must not be null"));
    }

    public List<SensorReading> getReadingsBetween(String plantId, LocalDateTime from, LocalDateTime to) {
        return sensorReadingRepository.findByPlantIdAndTimestampBetweenOrderByTimestampDesc(
                Objects.requireNonNull(plantId, "plantId must not be null"), from, to);
    }

    public SensorReading getLatest(String plantId) throws Exception {
        String safePlantId = Objects.requireNonNull(plantId, "plantId must not be null");
        return sensorReadingRepository.findFirstByPlantIdOrderByTimestampDesc(safePlantId)
                .orElseThrow(() -> new Exception("Aucune lecture pour plantId=" + safePlantId));
    }
}