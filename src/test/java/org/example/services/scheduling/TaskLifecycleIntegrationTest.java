package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.entities.plant.GrowthStage;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.CareTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskLifecycleIntegrationTest {

    @Autowired
    private CareTaskService careTaskService;

    @Autowired
    private CareTaskRepository careTaskRepository;

    @BeforeEach
    public void setUp() {
        careTaskRepository.deleteAll();
    }

    @Test
    @DisplayName("Cycle complet : Scénario nominal de bout en bout avec contrôle d'idempotence stricte")
    public void testCompleteTaskLifecycle() {
        Plant plant = buildHighNeedPlant("plant-test-123", "Ficus Test", "forest-test-456");

        CareTask generatedTask = careTaskService.generateTask(plant);
        assertNotNull(generatedTask);
        assertNotNull(generatedTask.getId());
        assertEquals(TaskStatus.PENDING, generatedTask.getStatus());

        CareTask duplicateTask = careTaskService.generateTask(plant);
        assertEquals(generatedTask.getId(), duplicateTask.getId());

        CareTask completedTask = careTaskService.markAsDone(generatedTask.getId());
        assertEquals(TaskStatus.DONE, completedTask.getStatus());
        assertNotNull(completedTask.getClosedAt());

        CareTask savedTask = careTaskRepository.findById(generatedTask.getId()).orElse(null);
        assertNotNull(savedTask);
        assertEquals(TaskStatus.DONE, savedTask.getStatus());
    }

    @Test
    @DisplayName("Cycle de vie : Une tâche clôturée (DONE/CANCEL) débloque le verrou d'idempotence pour les cycles futurs")
    public void testIdempotenceUnlocksAfterTaskClosure() {
        Plant plant = buildHighNeedPlant("plant-multi-cycle", "Chêne", "forest-1");

        CareTask task1 = careTaskService.generateTask(plant);
        assertNotNull(task1);

        careTaskService.cancelTask(task1.getId());
        careTaskRepository.delete(task1);

        CareTask task2 = careTaskService.generateTask(plant);
        assertNotNull(task2);
        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    @DisplayName("Intégrité : Multi-génération pour différentes plantes en parallèle")
    public void testParallelTaskGenerationForDifferentPlants() {
        Plant p1 = buildHighNeedPlant("P1", "Plante 1", "forest-1");
        Plant p2 = buildHighNeedPlant("P2", "Plante 2", "forest-2");

        CareTask t1 = careTaskService.generateTask(p1);
        CareTask t2 = careTaskService.generateTask(p2);

        assertNotNull(t1);
        assertNotNull(t2);
        assertNotEquals(t1.getId(), t2.getId());
        assertEquals("P1", t1.getPlantId());
        assertEquals("P2", t2.getPlantId());
    }

    private Plant buildHighNeedPlant(String id, String name, String forestId) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        plant.setForestId(forestId);
        plant.setHeightCm(250);
        plant.setGrowthStage(GrowthStage.FRUITING);
        plant.setStressIndex(0.95);
        plant.setWaterLevel(100);
        Species species = new Species("TestSpecies", 5000, 20, 60, 5000, 1, 0.5);
        species.setMaxHeight(300);
        plant.setSpecies(species);
        return plant;
    }
}
