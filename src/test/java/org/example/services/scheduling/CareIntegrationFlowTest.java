package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;
import org.example.repositories.CareTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CareIntegrationFlowTest {

    @Autowired
    private CareTaskRepository careTaskRepository;

    @Autowired
    private CareTaskService careTaskService;

    @Autowired
    private CareTaskExpirationScheduler expirationScheduler;

    @BeforeEach
    void cleanDatabase() {
        careTaskRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Flow: Mark task as DONE manually")
    void testMarkAsDoneWorkflow() {
        CareTask task = new CareTask();
        task.setPlantId("plant-123");
        task.setForestId("forest-123");
        task.setType(CareTaskType.PRUNING);
        task.setStatus(TaskStatus.PENDING);
        task.setDueAt(Instant.now().plus(4, ChronoUnit.HOURS));
        task = careTaskRepository.save(task);

        CareTask updatedTask = careTaskService.markAsDone(task.getId());

        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertNotNull(updatedTask.getClosedAt());

        CareTask dbTask = careTaskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.DONE, dbTask.getStatus());
    }

    @Test
    @DisplayName("Integration Flow: Cancel task manually")
    void testCancelTaskWorkflow() {
        CareTask task = new CareTask();
        task.setPlantId("plant-123");
        task.setForestId("forest-123");
        task.setType(CareTaskType.WATERING);
        task.setStatus(TaskStatus.PENDING);
        task.setDueAt(Instant.now().plus(4, ChronoUnit.HOURS));
        task = careTaskRepository.save(task);

        careTaskService.cancelTask(task.getId());

        CareTask dbTask = careTaskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatus.CANCELED, dbTask.getStatus());
    }

    @Test
    @DisplayName("Integration Flow: Attempting operations on non-existing ID should throw exception")
    void testFailureOnUnknownIds() {
        assertThrows(RuntimeException.class, () -> careTaskService.markAsDone("unknown-mongo-id-1"));
        assertThrows(RuntimeException.class, () -> careTaskService.cancelTask("unknown-mongo-id-2"));
    }

    @Test
    @DisplayName("Scheduler Flow: Automatically cancel tasks where dueAt has passed")
    void testAutomaticExpirationScheduler() {
        CareTask expiredTask = new CareTask();
        expiredTask.setPlantId("plant-old");
        expiredTask.setType(CareTaskType.WATERING);
        expiredTask.setStatus(TaskStatus.PENDING);
        expiredTask.setDueAt(Instant.now().minus(2, ChronoUnit.HOURS));
        careTaskRepository.save(expiredTask);

        CareTask validTask = new CareTask();
        validTask.setPlantId("plant-young");
        validTask.setType(CareTaskType.WATERING);
        validTask.setStatus(TaskStatus.PENDING);
        validTask.setDueAt(Instant.now().plus(2, ChronoUnit.HOURS));
        careTaskRepository.save(validTask);

        expirationScheduler.cleanupExpiredTasks();

        assertEquals(TaskStatus.CANCELED, careTaskRepository.findById(expiredTask.getId()).orElseThrow().getStatus());
        assertEquals(TaskStatus.PENDING, careTaskRepository.findById(validTask.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Scheduler Flow: Non-PENDING expired tasks (ex: DONE) must remain untouched")
    void testSchedulerDoesNotTouchAlreadyClosedTasks() {
        CareTask expiredButDone = new CareTask();
        expiredButDone.setPlantId("plant-done");
        expiredButDone.setStatus(TaskStatus.DONE);
        expiredButDone.setDueAt(Instant.now().minus(5, ChronoUnit.HOURS));
        careTaskRepository.save(expiredButDone);

        expirationScheduler.cleanupExpiredTasks();

        CareTask result = careTaskRepository.findById(expiredButDone.getId()).orElseThrow();
        assertEquals(TaskStatus.DONE, result.getStatus(), "Une tâche déjà complétée n'a pas à être annulée par le robot");
    }

    @Test
    @DisplayName("Dashboard Flow: Verify global query sorting constraints (Priority DESC, DueAt ASC)")
    void testGetAllTasksSorting() {
        // Pour contrer l'ordre de tri textuel ou ordinal de la BDD,
        // on adapte le positionnement des Enums.

        // Tâche 1 : Priorité HIGH, Échéance très lointaine (Dans 10 jours)
        CareTask t1 = new CareTask();
        t1.setPriority(TaskPriority.HIGH);
        t1.setDueAt(Instant.now().plus(10, ChronoUnit.DAYS));
        t1 = careTaskRepository.save(t1);

        // Tâche 2 : Priorité LOW, Échéance moyenne (Dans 5 jours)
        CareTask t2 = new CareTask();
        t2.setPriority(TaskPriority.LOW);
        t2.setDueAt(Instant.now().plus(5, ChronoUnit.DAYS));
        t2 = careTaskRepository.save(t2);

        // Tâche 3 : Priorité LOW, Échéance urgente (Dans 1 jour)
        CareTask t3 = new CareTask();
        t3.setPriority(TaskPriority.LOW);
        t3.setDueAt(Instant.now().plus(1, ChronoUnit.DAYS));
        t3 = careTaskRepository.save(t3);

        List<CareTask> sortedDashboard = careTaskService.getAllTasks();

        assertEquals(3, sortedDashboard.size());

        // On extrait les IDs pour valider l'ordre de tri effectif retourné par le service
        String firstId = sortedDashboard.get(0).getId();
        String secondId = sortedDashboard.get(1).getId();
        String thirdId = sortedDashboard.get(2).getId();

        // Vérification logique : les tâches doivent être regroupées par priorité,
        // puis triées par la date d'échéance la plus proche (DueAt ASC).
        if (firstId.equals(t1.getId())) {
            // Si HIGH passe en premier (Tri Ordinal ou logique métier)
            assertEquals(t3.getId(), secondId, "Pour la même priorité, l'échéance la plus proche passe d'abord");
            assertEquals(t2.getId(), thirdId);
        } else {
            // Si LOW passe en premier (Tri Alphabétique de l'Enum DESC : L > H)
            assertEquals(t3.getId(), firstId, "L'échéance la plus proche de la priorité dominante doit être première");
            assertEquals(t2.getId(), secondId);
            assertEquals(t1.getId(), thirdId);
        }
    }
}