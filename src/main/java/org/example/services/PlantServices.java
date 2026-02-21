package org.example.services;

import org.example.entites.plant.Plant;
import org.example.entites.species.Species;
import org.example.entites.plant.PlantState;
import org.example.entites.environment.EnvironmentData;
import org.example.entites.Intervention;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlantServices {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    // ❌ ON A SUPPRIMÉ EffectService ICI POUR EVITER L'ERREUR

    // --- CREATION ---

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

    // --- GESTION ---

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

    // --- LOGIQUE D'EVOLUTION (SIMPLIFIÉE POUR QUE CA MARCHE) ---

    public void evolvePlant(Plant plant, EnvironmentData env) {
        // 1. Valeurs environnementales
        double currentTemp = env.getTemperature();
        double currentHumidity = env.getHumidity();
        double currentLux = env.getLux();
        double currentWater = plant.getWaterLevel();

        // 2. Calcul du stress
        double waterStress = Math.abs(currentWater - plant.getSpecies().getOptimalWaterNeeds())
                / plant.getSpecies().getOptimalWaterNeeds();
        double tempStress = Math.abs(currentTemp - plant.getSpecies().getOptimalTemperature()) / 20.0;
        double humidityStress = Math.abs(currentHumidity - plant.getSpecies().getOptimalHumidity()) / 50.0;
        double luxStress = Math.abs(currentLux - plant.getSpecies().getOptimalLuxNeeds())
                / plant.getSpecies().getOptimalLuxNeeds();

        double totalStress = (waterStress + tempStress + humidityStress + luxStress) / 4.0;

        // 3. Mise à jour stress index
        double newStress = plant.getStressIndex() + totalStress * 0.2;
        plant.setStressIndex(Math.min(1.0, Math.max(0.0, newStress)));

        // 4. Mise à jour état
        updatePlantState(plant);

        // 5. Croissance — désormais toujours > 0, réduite si stress
        double stressFactor = 1.0 - plant.getStressIndex(); // plus stressé = moins croissance
        double growth = plant.getSpecies().getBaseGrowthRate() * stressFactor;

        // On garantit un minimum de croissance, même très stressé
        growth = Math.max(growth, plant.getSpecies().getBaseGrowthRate() * 0.05);

        plant.setHeightCm(plant.getHeightCm() + growth);

        // 6. Sécheresse naturelle
        plant.setWaterLevel(Math.max(0, plant.getWaterLevel() - 2.0));

        // 7. Sauvegarde
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
        switch(action.getType()) {
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

    // --- METHODES INDISPENSABLES POUR LE DASHBOARD ---

    public Plant savePlant(Plant plant) {
        return plantRepository.save(plant);
    }

    public List<Plant> getRecentPlants() {
        // C'est cette méthode qui affiche la tomate sur le Dashboard !
        return plantRepository.findTop10ByOrderByIdDesc();
    }
}