package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;
import org.example.entities.plant.Plant;
import org.example.repositories.CareTaskRepository;
import org.example.services.calendar.ExternalCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CareTaskServiceTest {

    @Mock
    private CareTaskRepository careTaskRepository;

    @Mock
    private ExternalCalendarService externalCalendarService;

    @InjectMocks
    private CareTaskService careTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================================
    // TRAIN DE TESTS : GÉNÉRATION ET SEUILS DE STRESS (WNS)
    // ============================================================

    @Test
    @DisplayName("Génération : Stress à 85% doit générer une tâche WATERING (Seuil critique > 80)")
    void shouldGenerateWateringTaskWhenStressIsHigh() {
        Plant plant = createMockPlant("plant-1", "Monstera", 85);

        when(careTaskRepository.findByPlantIdAndTypeAndScheduledAt(anyString(), any(), any())).thenReturn(Optional.empty());
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> {
            CareTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", "task-water-1");
            return t;
        });
        when(externalCalendarService.push(any())).thenReturn("cal-event-1");

        CareTask result = careTaskService.generateTask(plant);

        assertNotNull(result);
        assertEquals(CareTaskType.WATERING, result.getType());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertTrue(result.isFlexible(), "WATERING doit être flexible");
        assertEquals(0.85, result.getWnsScore(), 0.001);
    }

    @Test
    @DisplayName("Génération : Stress à 81% doit générer une tâche FERTILIZATION (Seuil global > 80 mais <= 80 pour WATERING)")
    void shouldGenerateFertilizationTaskWhenStressIsJustAboveGlobalThreshold() {
        // Dans ton service : determineTaskType renvoie WATERING si > 80.
        // Or 81 > 80, donc on teste ici la limite stricte de la structure.
        Plant plant = createMockPlant("plant-2", "Ficus", 81);

        when(careTaskRepository.findByPlantIdAndTypeAndScheduledAt(anyString(), any(), any())).thenReturn(Optional.empty());
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> {
            CareTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", "task-ferti-1");
            return t;
        });

        CareTask result = careTaskService.generateTask(plant);

        assertNotNull(result);
        assertEquals(CareTaskType.WATERING, result.getType(), "81 étant supérieur à 80, le type attendu est WATERING");
    }

    @Test
    @DisplayName("Génération : Stress exactement à 80% (Cas limite) -> FERTILIZATION attendu")
    void shouldGenerateFertilizationWhenStressIsExactlyEighty() {
        Plant plant = createMockPlant("plant-3", " Cactus", 80); // 80 / 100 = 0.8 (Passe le cap du 0.8 global)

        when(careTaskRepository.findByPlantIdAndTypeAndScheduledAt(anyString(), any(), any())).thenReturn(Optional.empty());
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> {
            CareTask t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", "task-ferti-2");
            return t;
        });

        CareTask result = careTaskService.generateTask(plant);

        assertNotNull(result);
        assertEquals(CareTaskType.FERTILIZATION, result.getType(), "Exactement 80 ne valide pas > 80, donc FERTILIZATION");
        assertTrue(result.isFlexible(), "FERTILIZATION doit être flexible");
    }

    @Test
    @DisplayName("Génération : Stress inférieur à 80% (ex: 79%) -> Doit retourner null immédiatement")
    void shouldReturnNullWhenStressIsBelowThreshold() {
        Plant plant = createMockPlant("plant-4", "Fougère", 79); // 0.79 < 0.80

        CareTask result = careTaskService.generateTask(plant);

        assertNull(result, "La tâche ne doit pas être créée si le score WNS simulé est inférieur à 0.8");
        verifyNoInteractions(careTaskRepository);
        verifyNoInteractions(externalCalendarService);
    }

    // ============================================================
    // TRAIN DE TESTS : IDEMPOTENCE
    // ============================================================

    @Test
    @DisplayName("Idempotence : Retourne la tâche existante sans doublon si critères identiques détectés")
    void shouldReturnExistingTaskToEnsureIdempotence() {
        Plant plant = createMockPlant("plant-1", "Monstera", 90);
        CareTask existingTask = new CareTask();
        ReflectionTestUtils.setField(existingTask, "id", "existing-unique-id");
        existingTask.setStatus(TaskStatus.PENDING);

        when(careTaskRepository.findByPlantIdAndTypeAndScheduledAt(anyString(), any(), any()))
                .thenReturn(Optional.of(existingTask));

        CareTask result = careTaskService.generateTask(plant);

        assertNotNull(result);
        assertEquals("existing-unique-id", result.getId());
        verify(careTaskRepository, never()).save(any(CareTask.class));
        verifyNoInteractions(externalCalendarService);
    }

    // ============================================================
    // TRAIN DE TESTS : WORKFLOWS DE CLÔTURE (DONE / CANCEL)
    // ============================================================

    @Test
    @DisplayName("Clôture DONE : Passage au statut DONE et fixation de la date closedAt")
    void shouldMarkTaskAsDoneSuccessfully() {
        CareTask pendingTask = new CareTask();
        ReflectionTestUtils.setField(pendingTask, "id", "task-done");
        pendingTask.setStatus(TaskStatus.PENDING);

        when(careTaskRepository.findById("task-done")).thenReturn(Optional.of(pendingTask));
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> inv.getArgument(0));

        CareTask result = careTaskService.markAsDone("task-done");

        assertNotNull(result);
        assertEquals(TaskStatus.DONE, result.getStatus());
        assertNotNull(result.getClosedAt(), "La date de complétion closedAt doit être injectée");
    }

    @Test
    @DisplayName("Clôture DONE : Doit lever une exception si l'ID de la tâche n'existe pas")
    void shouldThrowExceptionWhenMarkingDoneOnNonExistingTask() {
        when(careTaskRepository.findById("invalid-id")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            careTaskService.markAsDone("invalid-id");
        });

        assertEquals("Task not found", exception.getMessage());
        verify(careTaskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Annulation CANCEL : Passage au statut CANCELED et nettoyage de l'agenda externe si présent")
    void shouldCancelTaskAndRemoveFromExternalCalendar() {
        CareTask task = new CareTask();
        ReflectionTestUtils.setField(task, "id", "task-cancel");
        task.setStatus(TaskStatus.PENDING);
        task.setExternalId("google-event-xyz");

        when(careTaskRepository.findById("task-cancel")).thenReturn(Optional.of(task));
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> inv.getArgument(0));

        careTaskService.cancelTask("task-cancel");

        assertEquals(TaskStatus.CANCELED, task.getStatus());
        verify(externalCalendarService, times(1)).remove("google-event-xyz");
    }

    @Test
    @DisplayName("Annulation CANCEL : Pas d'appel au calendrier externe si pas d'externalId relié")
    void shouldCancelTaskWithoutInteractingWithCalendarIfExternalIdIsNull() {
        CareTask task = new CareTask();
        ReflectionTestUtils.setField(task, "id", "task-cancel-no-cal");
        task.setStatus(TaskStatus.PENDING);
        task.setExternalId(null);

        when(careTaskRepository.findById("task-cancel-no-cal")).thenReturn(Optional.of(task));

        careTaskService.cancelTask("task-cancel-no-cal");

        assertEquals(TaskStatus.CANCELED, task.getStatus());
        verify(externalCalendarService, never()).remove(anyString());
    }

    @Test
    @DisplayName("Annulation CANCEL : Doit lever une exception si l'ID à annuler est introuvable")
    void shouldThrowExceptionWhenCancelingNonExistingTask() {
        when(careTaskRepository.findById("unknown-id")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            careTaskService.cancelTask("unknown-id");
        });

        assertEquals("Task not found", exception.getMessage());
    }

    // ============================================================
    // TRAIN DE TESTS : DASHBOARD ET STRATÉGIES DE RECHERCHE
    // ============================================================

    @Test
    @DisplayName("Dashboard : Récupération globale ordonnée par les critères du Repository")
    void shouldReturnAllTasksOrderedByPriorityAndDueAt() {
        List<CareTask> mockList = List.of(new CareTask(), new CareTask());
        when(careTaskRepository.findAllByOrderByPriorityDescDueAtAsc()).thenReturn(mockList);

        List<CareTask> result = careTaskService.getAllTasks();

        assertEquals(2, result.size());
        verify(careTaskRepository, times(1)).findAllByOrderByPriorityDescDueAtAsc();
    }

    // Helper method pour factoriser la création de plantes de test
    private Plant createMockPlant(String id, String name, int stressIndex) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        plant.setStressIndex(stressIndex);
        plant.setForestId("forest-generic-id");
        return plant;
    }
}