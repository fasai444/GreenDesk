package org.example;

import org.example.entities.environment.EnvironmentData;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.entities.Intervention;
import org.example.services.PlantService;
import org.example.services.SpeciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestPlantServices {

    private static final LocalDateTime TEST_TIME =
            LocalDateTime.of(2025, 1, 1, 12, 0);

    // helper dans PlantServicesTest
    private EnvironmentData env(double temperature, double humidity, double lux, double rainfall) {
        return new EnvironmentData(
                TEST_TIME,
                temperature,
                humidity,
                lux,
                rainfall
        );
    }

    @Autowired
    private PlantService plantServices;

    @Autowired
    private SpeciesService speciesServices;

    private Species species;

    @BeforeEach
    void setup() throws Exception {
        // Supprime toutes les plantes et espèces avant chaque test
        plantServices.deleteAllPlants();
        speciesServices.deleteAllSpecies();

        try {
            species = speciesServices.createSpecies(new Species("Tomate"));
        } catch (Exception e) {
            // Si elle existe déjà, on la récupère
            species = speciesServices.getSpeciesByName("Tomate").orElseThrow();
        }
    }

    // --- TESTS CREATION ---
    @Test
    void createPlant_success() throws Exception {
        Plant p = plantServices.createPlant("Plant1", species.getId());
        assertNotNull(p.getId());
        assertEquals(species.getId(), p.getSpecies().getId());
    }

    @Test
    void createPlant_invalidSpecies_shouldFail() {
        Exception ex = assertThrows(Exception.class, () ->
                plantServices.createPlant("BadPlant", "invalid-id")
        );
        assertTrue(ex.getMessage().contains("Espèce introuvable"));
    }

    // --- TESTS SUPPRESSION ---
    @Test
    void deletePlantById_shouldRemovePlantFromDB() throws Exception {
        Plant p = plantServices.createPlant("ToDelete", species.getId());
        String plantId = p.getId();

        // Vérifie qu'elle existe
        assertNotNull(plantServices.getPlantById(plantId).orElse(null));

        // Supprime la plante
        plantServices.deletePlantById(plantId);

        // Vérifie qu'elle n'existe plus
        assertTrue(plantServices.getPlantById(plantId).isEmpty());
    }

    // --- TESTS EVOLUTION ---
    @Test
    void evolvePlant_stressIndexAlwaysBetween0And1() {
        Plant plant = new Plant("Test", species);
        EnvironmentData extremeEnv = env(80, 5, 15000, 0);
        plantServices.evolvePlant(plant, extremeEnv);
        assertTrue(plant.getStressIndex() >= 0.0);
        assertTrue(plant.getStressIndex() <= 1.0);
    }

    @Test
    void optimalEnvironment_shouldKeepPlantHealthy() {
        Plant plant = new Plant("HealthyPlant", species);
        EnvironmentData optimalEnv = env(
                species.getOptimalTemperature(),
                species.getOptimalHumidity(),
                species.getOptimalLuxNeeds(),
                5
        );
        plantServices.evolvePlant(plant, optimalEnv);
        assertEquals(PlantState.HEALTHY, plant.getPlantState());
        assertTrue(plant.getStressIndex() < 0.3);
    }

    @Test
    void hostileEnvironment_shouldIncreaseStressIndex() {
        Plant plant = new Plant("StressedPlant", species);
        EnvironmentData hostileEnv = env(
                species.getOptimalTemperature() + 40,
                5,
                species.getOptimalLuxNeeds() * 3,
                0
        );

        double oldStress = plant.getStressIndex();

        // On simule plusieurs ticks pour que le stress augmente significativement
        for (int i = 0; i < 10; i++) {
            plantServices.evolvePlant(plant, hostileEnv);
        }

        double newStress = plant.getStressIndex();

        assertTrue(newStress > oldStress, "Le stressIndex doit augmenter dans un environnement hostile");

        assertTrue(
                plant.getPlantState() == PlantState.STRESSED
                        || plant.getPlantState() == PlantState.DORMANT
                        || plant.getPlantState() == PlantState.DISEASED,
                "L'état de la plante doit refléter un stress élevé après plusieurs ticks"
        );
    }

    @Test
    void plantGrowsIfHealthy() {
        Plant plant = new Plant("Grow", species);
        EnvironmentData goodEnv = env(
                species.getOptimalTemperature(),
                species.getOptimalHumidity(),
                species.getOptimalLuxNeeds(),
                10
        );
        double before = plant.getHeightCm();
        plantServices.evolvePlant(plant, goodEnv);
        assertTrue(plant.getHeightCm() >= before);
    }

    @Test
    void veryHighStressLeadsToHighStressIndex() {
        Plant plant = new Plant("ExtremePlant", species);
        EnvironmentData extremeEnv = env(90, 0, 20000, 0);

        double oldStress = plant.getStressIndex();

        // Plusieurs ticks pour faire monter le stress
        for (int i = 0; i < 20; i++) {
            plantServices.evolvePlant(plant, extremeEnv);
        }

        double newStress = plant.getStressIndex();

        assertTrue(newStress > oldStress, "Le stressIndex doit augmenter dans un environnement extrême");
        assertTrue(newStress > 0.8, "Le stressIndex doit être très élevé après plusieurs ticks en environnement extrême");
    }


    @Test
    void plantsAlwaysGrowUnderVariousEnvironments() throws Exception {
        List<Plant> plants = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            plants.add(plantServices.createPlant("GrowthPlant" + i, species.getId()));
        }

        EnvironmentData hostileEnv = new EnvironmentData(
                TEST_TIME,
                species.getOptimalTemperature() + 50,
                5,
                species.getOptimalLuxNeeds() * 3,
                0
        );

        EnvironmentData optimalEnv = new EnvironmentData(
                TEST_TIME,
                species.getOptimalTemperature(),
                species.getOptimalHumidity(),
                species.getOptimalLuxNeeds(),
                5
        );

        EnvironmentData randomEnv = new EnvironmentData();

        List<EnvironmentData> environments = List.of(hostileEnv, optimalEnv, randomEnv);

        for (EnvironmentData env : environments) {
            for (Plant plant : plants) {
                double initialHeight = plant.getHeightCm();
                plantServices.evolvePlant(plant, env);
                assertTrue(plant.getHeightCm() > initialHeight,
                        "La plante " + plant.getName() + " doit toujours grandir");
            }
        }
    }

    // --- TESTS INTERVENTIONS ---
    @Test
    void waterIntervention_shouldIncreaseWaterLevel() {
        Plant plant = new Plant("P", species);
        double before = plant.getWaterLevel();
        Intervention water = new Intervention(Intervention.InterventionType.WATER, 10);
        plantServices.applyIntervention(plant, water);
        assertEquals(before + 10, plant.getWaterLevel());
    }

    @Test
    void prune_shouldReduceHeightButNotBelowZero() {
        Plant plant = new Plant("P", species);
        plant.setHeightCm(5);
        Intervention prune = new Intervention(Intervention.InterventionType.PRUNE, 10);
        plantServices.applyIntervention(plant, prune);
        assertEquals(0, plant.getHeightCm());
    }

    @Test
    void shading_shouldReduceLuxButNotBelowZero() {
        Plant plant = new Plant("P", species);
        plant.setLux(20);
        Intervention shading = new Intervention(Intervention.InterventionType.SHADING, 25);
        plantServices.applyIntervention(plant, shading);
        assertEquals(0, plant.getLux());
    }
}
