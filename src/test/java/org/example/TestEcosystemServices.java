package org.example;

import org.example.entites.ecosystem.diseases.BacterialDisease;
import org.example.entites.ecosystem.diseases.MildiouDisease;
import org.example.entites.ecosystem.diseases.PlantDisease;
import org.example.entites.ecosystem.diseases.RustDisease;
import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;
import org.example.entites.species.Species;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.example.services.EcosystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestEcosystemServices {

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;
    @Autowired
    private EcosystemService ecosystemService;

    private org.example.entites.ecosystem.Ecosystem ecosystem;
    private Plant healthyPlant;
    private Plant stressedPlant;
    private Forest forest;

    @BeforeEach
    void setup() {
        plantRepository.deleteAll();

        // Création des plantes
        Species species = new Species("TestSpecies");
        species.setOptimalTemperature(22.0);
        species.setOptimalHumidity(80.0);
        species.setOptimalLuxNeeds(3000.0);
        speciesRepository.save(species);

        healthyPlant = plantRepository.save(new Plant("HealthyPlant", species));
        stressedPlant = plantRepository.save(new Plant("StressedPlant", species));

        // Création de la forêt et des cellules
        Forest.ForestCell cell1 = new Forest.ForestCell(0, 0, healthyPlant.getId());
        Forest.ForestCell cell2 = new Forest.ForestCell(0, 1, stressedPlant.getId());

        Forest forest = new Forest("TestForest", 2, 2);
        forest.setCells(List.of(cell1, cell2));

        ecosystem = new org.example.entites.ecosystem.Ecosystem(forest);
        ecosystem.initialiseEcosystem();

        // Injecter l’écosystème dans le service
        ecosystemService.setEcosystem(ecosystem);
    }

    @Test
    void testProgressDisease_CellWithDisease_UpdatesPlant() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(0);
        MildiouDisease disease = new MildiouDisease(0.5);
        cell.infect(disease);

        ecosystemService.tick();

        Plant p = plantRepository.findById(healthyPlant.getId()).orElseThrow();
        assertNotNull(p.getHeightCm());
        assertNotNull(p.getStressIndex());
    }

    @Test
    void testInfection_ScenarioThresholdNotReached() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(0);

        ecosystemService.tick();

        assertFalse(cell.isDiseased());
    }

    @Test
    void testInfection_ScenarioThresholdReached() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(0);
        org.example.entites.ecosystem.EcosystemCell neighbor = ecosystem.getCells().get(1);

        neighbor.infect(new MildiouDisease(0.7));

        ecosystemService.tick();

        assertTrue(cell.isDiseased());
        assertNotNull(cell.getDisease());
        assertEquals("Mildiou", cell.getDisease().getName());
    }

    @Test
    void testRecovery_CellRecoversIfThresholdReached() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(0);
        cell.infect(new BacterialDisease(0.8));

        ecosystemService.tick();

        assertFalse(cell.isDiseased());
    }

    @Test
    void testPlantRepositoryIsCalledOnProgress() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(1);
        cell.infect(new RustDisease(0.5));

        ecosystemService.tick();

        Plant p = plantRepository.findById(stressedPlant.getId()).orElseThrow();
        assertNotNull(p.getStressIndex());
        assertNotNull(p.getHeightCm());
    }

    @Test
    void testMajorityDiseaseSelection() {
        org.example.entites.ecosystem.EcosystemCell cell = ecosystem.getCells().get(0);
        org.example.entites.ecosystem.EcosystemCell neighbor = ecosystem.getCells().get(1);

        neighbor.infect(new MildiouDisease(0.3));

        // Création d’une instance de Species
        Species genericSpecies = speciesRepository.save(new Species("GenericSpecies"));

        // Création d’un autre voisin avec BacterialDisease plus sévère
        Plant extraPlant = plantRepository.save(new Plant("ExtraPlant", genericSpecies));
        String extraPlantId = extraPlant.getId();

        Forest.ForestCell extraCell = new Forest.ForestCell(1, 1, extraPlantId);
        org.example.entites.ecosystem.EcosystemCell neighbor2 = new org.example.entites.ecosystem.EcosystemCell(extraCell);
        neighbor2.infect(new BacterialDisease(0.7));

        ecosystem.getCells().add(neighbor2);

        PlantDisease majority = cell.getMostSevereNeighborDisease(ecosystem);

        assertNotNull(majority);
        assertEquals("BacterialWilt", majority.getName());
    }

    @Test
    void testSimulateTicks() {
        org.example.entites.ecosystem.EcosystemCell cell1 = ecosystem.getCells().get(0);
        org.example.entites.ecosystem.EcosystemCell cell2 = ecosystem.getCells().get(1);

        cell2.infect(new MildiouDisease(0.6));

        ecosystemService.simulateTicks(5);

        assertTrue(cell1.isDiseased());
        PlantDisease disease = cell1.getDisease();
        assertNotNull(disease);
        assertTrue(disease.getSeverity() > 0);
    }
}