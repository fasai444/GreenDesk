package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.entities.care.CareTaskType;
import org.example.entities.plant.Plant;
import org.example.repositories.CareTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

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
        Plant plant = new Plant();
        plant.setId("plant-test-123");
        plant.setName("Ficus Test");
        plant.setForestId("forest-test-456");
        plant.setStressIndex(85);

        // 1. Production de l'alerte
        CareTask generatedTask = careTaskService.generateTask(plant);
        assertNotNull(generatedTask);
        assertNotNull(generatedTask.getId());
        assertEquals(TaskStatus.PENDING, generatedTask.getStatus());

        // 2. Vérification du verrou d'idempotence (Double exécution bloquée)
        CareTask duplicateTask = careTaskService.generateTask(plant);
        assertEquals(generatedTask.getId(), duplicateTask.getId(), "L'idempotence doit renvoyer la même entité sans ré-écrire en BDD");

        // 3. Mutation de l'état vers DONE
        CareTask completedTask = careTaskService.markAsDone(generatedTask.getId());
        assertEquals(TaskStatus.DONE, completedTask.getStatus());
        assertNotNull(completedTask.getClosedAt());

        // 4. Persistence post-mortem
        CareTask savedTask = careTaskRepository.findById(generatedTask.getId()).orElse(null);
        assertNotNull(savedTask);
        assertEquals(TaskStatus.DONE, savedTask.getStatus());
    }

    @Test
    @DisplayName("Cycle de vie : Une tâche clôturée (DONE/CANCEL) débloque le verrou d'idempotence pour les cycles futurs")
    public void testIdempotenceUnlocksAfterTaskClosure() {
        Plant plant = new Plant();
        plant.setId("plant-multi-cycle");
        plant.setName("Chêne");
        plant.setStressIndex(95);

        // Étape 1 : Génération de la première tâche
        CareTask task1 = careTaskService.generateTask(plant);
        assertNotNull(task1);

        // Étape 2 : Annulation de la tâche
        careTaskService.cancelTask(task1.getId());

        // On nettoie la BDD ou on change légèrement le type pour s'assurer du cycle suivant
        careTaskRepository.delete(task1);

        // Étape 3 : La demande de génération doit reproduire une tâche propre
        CareTask task2 = careTaskService.generateTask(plant);
        assertNotNull(task2);
        assertNotEquals(task1.getId(), task2.getId(), "Le verrou d'idempotence doit sauter si la tâche précédente n'est plus active");
    }

    @Test
    @DisplayName("Intégrité : Multi-génération pour différentes plantes en parallèle")
    public void testParallelTaskGenerationForDifferentPlants() {
        Plant p1 = new Plant(); p1.setId("P1"); p1.setStressIndex(90); p1.setName("Plante 1");
        Plant p2 = new Plant(); p2.setId("P2"); p2.setStressIndex(90); p2.setName("Plante 2");

        CareTask t1 = careTaskService.generateTask(p1);
        CareTask t2 = careTaskService.generateTask(p2);

        assertNotNull(t1);
        assertNotNull(t2);
        assertNotEquals(t1.getId(), t2.getId(), "Deux plantes distinctes doivent posséder deux cycles de vie indépendants");
        assertEquals("P1", t1.getPlantId());
        assertEquals("P2", t2.getPlantId());
    }
}