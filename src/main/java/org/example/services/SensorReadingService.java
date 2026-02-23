package org.example.services;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entites.SensorReading;
import org.example.entites.plant.Plant;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorReadingService {

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private PlantAlertService plantAlertService;

    public SensorReading addReading(String plantId, CreateSensorReadingRequest req) throws Exception {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new Exception("Plante introuvable: " + plantId));

        LocalDateTime ts = req.getTimestamp() != null ? req.getTimestamp() : LocalDateTime.now();

        SensorReading reading = new SensorReading(
                plantId,
                ts,
                req.getTemperature(),
                req.getHumidity(),
                req.getLux(),
                req.getRainfall()
        );

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
        return sensorReadingRepository.findByPlantIdOrderByTimestampDesc(plantId);
    }

    public List<SensorReading> getReadingsBetween(String plantId, LocalDateTime from, LocalDateTime to) {
        return sensorReadingRepository.findByPlantIdAndTimestampBetweenOrderByTimestampDesc(plantId, from, to);
    }

    public SensorReading getLatest(String plantId) throws Exception {
        return sensorReadingRepository.findFirstByPlantIdOrderByTimestampDesc(plantId)
                .orElseThrow(() -> new Exception("Aucune lecture pour plantId=" + plantId));
    }
}