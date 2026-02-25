package org.example;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.entities.alert.PlantAlert;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.PlantAlertRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SensorReadingRepository;
import org.example.services.PlantService;
import org.example.services.SensorReadingService;
import org.example.services.SpeciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestSensorReadingsAndAlerts {

    @Autowired private PlantService plantServices;
    @Autowired private SpeciesService speciesServices;

    @Autowired private SensorReadingService sensorReadingService;

    @Autowired private SensorReadingRepository sensorReadingRepository;
    @Autowired private PlantAlertRepository plantAlertRepository;
    @Autowired private PlantRepository plantRepository;

    private Species species;
    private Plant plant;

    @BeforeEach
    void setup() throws Exception {
        plantAlertRepository.deleteAll();
        sensorReadingRepository.deleteAll();
        plantServices.deleteAllPlants();
        speciesServices.deleteAllSpecies();

        species = speciesServices.createSpecies(new Species("Tomate"));
        plant = plantServices.createPlant("Plant1", species.getId());
    }

    @Test
    void addReading_shouldStoreReading_andUpdatePlant() throws Exception {
        CreateSensorReadingRequest req = new CreateSensorReadingRequest();
        req.setTemperature(10);
        req.setHumidity(20);
        req.setLux(100);
        req.setRainfall(5);

        SensorReading saved = sensorReadingService.addReading(plant.getId(), req);

        assertNotNull(saved.getId());
        assertEquals(plant.getId(), saved.getPlantId());

        Plant updated = plantRepository.findById(plant.getId()).orElseThrow();
        assertEquals(10, updated.getTemperature(), 0.001);
        assertEquals(20, updated.getHumidity(), 0.001);
        assertEquals(100, updated.getLux(), 0.001);
    }

    @Test
    void extremeReading_shouldCreateAlerts() throws Exception {
        CreateSensorReadingRequest req = new CreateSensorReadingRequest();
        req.setTemperature(species.getOptimalTemperature() + 20);
        req.setHumidity(species.getOptimalHumidity() - 40);
        req.setLux(species.getOptimalLuxNeeds() * 0.2);
        req.setRainfall(0);

        sensorReadingService.addReading(plant.getId(), req);

        List<PlantAlert> activeAlerts = plantAlertRepository
                .findByPlantIdAndAcknowledgedFalseOrderByCreatedAtDesc(plant.getId());

        assertFalse(activeAlerts.isEmpty(), "Should create at least one alert");
    }
}