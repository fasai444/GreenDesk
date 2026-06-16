package org.example;

import org.example.entities.environment.EnvironmentData;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.services.*;
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
public class TestSimulationEnvironment {

    @Autowired
    private SpeciesService speciesServices;

    @Autowired
    private PlantService plantServices;

    @Autowired
    private EnvironmentService envService;

    @Autowired
    private Simulation simulation;

    private EnvironmentData environment;
    private List<Species> speciesList;

    @BeforeEach
    void setup() throws Exception {
        environment = new EnvironmentData(LocalDateTime.of(2025, 12, 15, 6, 0), 20, 50, 1000, 0);

        speciesList = new ArrayList<>();
        for (String name : new String[]{"SpeciesA", "SpeciesB"}) {
            try {
                speciesList.add(speciesServices.createSpecies(new Species(name)));
            } catch (Exception e) {
                speciesList.add(speciesServices.getSpeciesByName(name).orElseThrow());
            }
        }

        // Crée plusieurs plantes pour chaque espèce
        int plantCounter = 1;
        for (Species sp : speciesList) {
            for (int i = 0; i < 3; i++) { // 3 plantes par espèce
                Plant p = plantServices.createPlant("Plant" + plantCounter++, sp.getId());
                environment.addPlant(p);
            }
        }
    }

    // -----------------------------
    // TESTS ENVIRONMENT SERVICES
    // -----------------------------

    @Test
    void tick_shouldAdvanceOneHourAndEvolveAllPlants() throws Exception {
        LocalDateTime oldTime = environment.getTimestamp();
        List<Double> oldHeights = new ArrayList<>();
        for (Plant p : environment.getPlants()) oldHeights.add(p.getHeightCm());

        envService.tick(environment);

        assertEquals(oldTime.plusHours(1), environment.getTimestamp(), "Le timestamp doit avancer d'une heure");

        int i = 0;
        for (Plant p : environment.getPlants()) {
            assertNotNull(p.getPlantState(), "L'état de chaque plante doit être mis à jour");
            if (p.getPlantState() != PlantState.DORMANT && p.getPlantState() != PlantState.DISEASED) {
                assertTrue(p.getHeightCm() > oldHeights.get(i), "La plante doit pousser si elle n'est pas dormante ou malade");
            }
            i++;
        }
    }

    @Test
    void simulate_multipleHours_shouldAdvanceEnvironmentAndAllPlants() throws Exception {
        LocalDateTime oldTime = environment.getTimestamp();
        List<Double> oldHeights = new ArrayList<>();
        for (Plant p : environment.getPlants()) oldHeights.add(p.getHeightCm());

        envService.simulate(environment, 5); // simulate 5 heures

        assertEquals(oldTime.plusHours(5), environment.getTimestamp(), "Le timestamp doit avancer de 5 heures");

        int i = 0;
        for (Plant p : environment.getPlants()) {
            assertNotNull(p.getPlantState(), "Chaque plante doit avoir un état valide");
            if (p.getPlantState() != PlantState.DORMANT && p.getPlantState() != PlantState.DISEASED) {
                assertTrue(p.getHeightCm() >= oldHeights.get(i), "La plante doit évoluer");
            }
            i++;
        }
    }

    // -----------------------------
    // TESTS SIMULATION
    // -----------------------------

    @Test
    void simulation_manualSimulate_shouldAdvanceEnvironment() {
        LocalDateTime oldTime = simulation.getEnvironment().getTimestamp();
        simulation.simulate(3);
        assertEquals(oldTime.plusHours(3), simulation.getEnvironment().getTimestamp());
    }

    @Test
    void simulation_scheduler_shouldAdvanceEnvironment() {
        LocalDateTime oldTime = simulation.getEnvironment().getTimestamp();
        simulation.runHourlySimulation();
        assertEquals(oldTime.plusHours(1), simulation.getEnvironment().getTimestamp());
    }
}
