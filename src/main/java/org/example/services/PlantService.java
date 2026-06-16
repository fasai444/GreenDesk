package org.example.services;

import org.example.entities.Intervention;
import org.example.entities.environment.EnvironmentData;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlantService {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Autowired(required = false)
    private EffectService effectService;

    public Plant createPlant(String name, String speciesId,
                             double water, double temp, double humidity, double lux) throws Exception {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new Exception("Espèce introuvable : " + speciesId));
        Plant plant = new Plant(name, species, water, temp, humidity, lux);
        plant.setPlantState(plant.evaluateState());
        return plantRepository.save(plant);
    }

    public Plant createPlant(String name, String speciesId) throws Exception {
        Species species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new Exception("Espèce introuvable : " + speciesId));
        Plant plant = new Plant(name, species);
        plant.setPlantState(plant.evaluateState());
        return plantRepository.save(plant);
    }

    public void deletePlantById(String plantId) {
        plantRepository.deleteById(plantId);
    }

    public List<Plant> getAllPlants() {
        return plantRepository.findAll();
    }

    public void deleteAllPlants() {
        plantRepository.deleteAll();
    }

    public Optional<Plant> getPlantById(String id) throws Exception {
        return Optional.ofNullable(plantRepository.findById(id).orElse(null));
    }

    public void evolvePlant(Plant plant, EnvironmentData env) {
        double currentTemp = env.getTemperature();
        double currentHumidity = env.getHumidity();
        double currentLux = env.getLux();
        double currentWater = plant.getWaterLevel();

        double growthRateMultiplier = 1.0;
        double stressReduction = 0.0;

        if (effectService != null) {
            EffectService.EffectModifiers modifiers = effectService.calculateTotalModifiers(plant.getId());
            currentTemp += modifiers.temperature;
            currentHumidity += modifiers.humidity;
            currentLux += modifiers.lux;
            currentWater += modifiers.water;
            growthRateMultiplier += modifiers.growthRate;
            stressReduction += modifiers.stressReduction;
        }

        double waterStress = Math.abs(currentWater - plant.getSpecies().getOptimalWaterNeeds())
                / plant.getSpecies().getOptimalWaterNeeds();
        double tempStress = Math.abs(currentTemp - plant.getSpecies().getOptimalTemperature()) / 20.0;
        double humidityStress = Math.abs(currentHumidity - plant.getSpecies().getOptimalHumidity()) / 50.0;
        double luxStress = Math.abs(currentLux - plant.getSpecies().getOptimalLuxNeeds())
                / plant.getSpecies().getOptimalLuxNeeds();

        double totalStress = (waterStress + tempStress + humidityStress + luxStress) / 4.0;
        totalStress = Math.max(0.0, totalStress - stressReduction);

        double newStress = plant.getStressIndex() + totalStress * 0.2;
        plant.setStressIndex(Math.min(1.0, Math.max(0.0, newStress)));

        updatePlantState(plant);

        double stressFactor = 1.0 - plant.getStressIndex();
        double growth = plant.getSpecies().getBaseGrowthRate() * stressFactor * growthRateMultiplier;
        growth = Math.max(growth, plant.getSpecies().getBaseGrowthRate() * 0.05);

        plant.setHeightCm(plant.getHeightCm() + growth);
        plant.setWaterLevel(Math.max(0, plant.getWaterLevel() - 2.0));

        plantRepository.save(plant);
    }

    private void updatePlantState(Plant plant) {
        double stress = plant.getStressIndex();
        if (stress < 0.3) plant.setPlantState(PlantState.HEALTHY);
        else if (stress < 0.6) plant.setPlantState(PlantState.STRESSED);
        else if (stress < 0.9) plant.setPlantState(PlantState.DORMANT);
        else plant.setPlantState(PlantState.DISEASED);
    }

    public void applyIntervention(Plant plant, Intervention action) {
        switch (action.getType()) {
            case WATER:
                plant.setWaterLevel(plant.getWaterLevel() + action.getValue());
                break;
            case PRUNE:
                plant.setHeightCm(Math.max(0, plant.getHeightCm() - action.getValue()));
                break;
            case SHADING:
                plant.setLux(Math.max(0, plant.getLux() - action.getValue()));
                break;
        }
        plant.setPlantState(plant.evaluateState());
        plantRepository.save(plant);
    }

    public Plant savePlant(Plant plant) {
        return plantRepository.save(plant);
    }

    public List<Plant> getRecentPlants() {
        return plantRepository.findTop10ByOrderByIdDesc();
    }
}
