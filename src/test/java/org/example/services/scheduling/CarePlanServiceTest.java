package org.example.services.scheduling;

import org.example.entities.care.CarePlan;
import org.example.repositories.CarePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarePlanServiceTest {

    @Mock
    private CarePlanRepository carePlanRepository;

    @Mock
    private CareTaskService careTaskService;

    @InjectMocks
    private CarePlanService carePlanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(careTaskService.generateTasksForRequest(any())).thenReturn(java.util.List.of());
    }

    // ============================================================
    // TRAIN DE TESTS : OBTENTION / CRÉATION DE PLAN (getOrCreatePlan)
    // ============================================================

    @Test
    @DisplayName("getOrCreatePlan : Doit renvoyer le plan existant s'il est déjà en base")
    void shouldReturnExistingPlan() {
        CarePlan planExistant = new CarePlan("plant-100");
        when(carePlanRepository.findByPlantId("plant-100")).thenReturn(Optional.of(planExistant));

        CarePlan result = carePlanService.getOrCreatePlan("plant-100");

        assertNotNull(result);
        assertEquals("plant-100", result.getPlantId());
        verify(carePlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreatePlan : Doit instancier, sauvegarder et renvoyer un nouveau plan si inexistant")
    void shouldCreateAndSaveNewPlanWhenNotFound() {
        when(carePlanRepository.findByPlantId("plant-200")).thenReturn(Optional.empty());
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        CarePlan result = carePlanService.getOrCreatePlan("plant-200");

        assertNotNull(result);
        assertEquals("plant-200", result.getPlantId());
        verify(carePlanRepository, times(1)).save(any(CarePlan.class));
    }

    // ============================================================
    // TRAIN DE TESTS : AJOUT DE TÂCHES ET IDEMPOTENCE (addTaskToPlan)
    // ============================================================

    @Test
    @DisplayName("addTaskToPlan : Ajoute la tâche, met à jour le timestamp et sauvegarde si absente")
    void shouldAddTaskToPlanWhenNotPresent() {
        CarePlan plan = new CarePlan("plant-1");
        Instant avantAjout = Instant.now();

        when(carePlanRepository.findByPlantId("plant-1")).thenReturn(Optional.of(plan));
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        carePlanService.addTaskToPlan("plant-1", "task-abc");

        // Assertions sur l'état interne
        assertTrue(plan.getTaskIds().contains("task-abc"), "L'ID de la tâche doit être ajouté à la liste");
        verify(carePlanRepository, times(1)).save(plan);
    }

    @Test
    @DisplayName("addTaskToPlan : Bloque l'ajout (Idempotence) si la tâche est déjà enregistrée dans le plan")
    void shouldNotAddTaskToPlanIfAlreadyContainsTask() {
        CarePlan plan = new CarePlan("plant-1");
        plan.addTask("task-abc"); // Déjà là

        when(carePlanRepository.findByPlantId("plant-1")).thenReturn(Optional.of(plan));

        carePlanService.addTaskToPlan("plant-1", "task-abc");

        assertEquals(1, plan.getTaskIds().size(), "La liste ne doit pas grandir");
        verify(carePlanRepository, never()).save(any());
    }

    @Test
    @DisplayName("addTaskToPlan : Permet d'accumuler plusieurs tâches distinctes sans écrasement")
    void shouldAccumulateMultipleDifferentTasks() {
        CarePlan plan = new CarePlan("plant-1");
        plan.addTask("task-1");

        when(carePlanRepository.findByPlantId("plant-1")).thenReturn(Optional.of(plan));
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        carePlanService.addTaskToPlan("plant-1", "task-2");

        assertEquals(2, plan.getTaskIds().size(), "Le plan doit contenir les deux tâches");
        assertTrue(plan.getTaskIds().contains("task-1"));
        assertTrue(plan.getTaskIds().contains("task-2"));
        verify(carePlanRepository, times(1)).save(plan);
    }

    @Test
    @DisplayName("addTaskToPlan : Doit lever une exception ou propager l'erreur si la base échoue à la sauvegarde")
    void shouldPropagateExceptionWhenRepositoryFails() {
        CarePlan plan = new CarePlan("plant-1");
        when(carePlanRepository.findByPlantId("plant-1")).thenReturn(Optional.of(plan));
        when(carePlanRepository.save(any())).thenThrow(new RuntimeException("Database down"));

        assertThrows(RuntimeException.class, () -> {
            carePlanService.addTaskToPlan("plant-1", "task-fail");
        });
    }

    // ============================================================
    // TRAIN DE TESTS : RECALCUL GLOBAL (recomputeGlobalPlan)
    // ============================================================

    @Test
    @DisplayName("recomputeGlobalPlan : Force le recalcul (touchRecalculation) et persiste si l'id de la plante est fourni")
    void shouldForceRecomputeGlobalPlanWhenPlantIdIsProvided() {
        CarePlan plan = new CarePlan("plant-777");
        when(carePlanRepository.findByPlantId("plant-777")).thenReturn(Optional.of(plan));
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        carePlanService.recomputeGlobalPlan("forest-123", "plant-777");

        verify(carePlanRepository, times(1)).save(plan);
    }

    @Test
    @DisplayName("recomputeGlobalPlan : génère les tâches même si plantId est null (forêt entière)")
    void shouldGenerateTasksForForestWhenPlantIdIsNull() {
        carePlanService.recomputeGlobalPlan("forest-123", null);

        verify(careTaskService).generateTasksForRequest(any());
        verifyNoInteractions(carePlanRepository);
    }

    @Test
    @DisplayName("recomputeGlobalPlan : Crée un plan à la volée s'il n'existait pas avant le recalcul")
    void shouldCreateNewPlanIfMissingDuringRecompute() {
        when(carePlanRepository.findByPlantId("plant-new")).thenReturn(Optional.empty());
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        carePlanService.recomputeGlobalPlan("forest-123", "plant-new");

        // getOrCreatePlan fait un premier save, puis recomputeGlobalPlan fait le deuxième save.
        verify(carePlanRepository, times(2)).save(any(CarePlan.class));
    }

    @Test
    @DisplayName("Edge Case : Comportement nominal avec des chaînes vides comme identifiants")
    void shouldHandleEmptyStringsAsValidIdentifiersInDatabaseContext() {
        CarePlan planIdVide = new CarePlan("");
        when(carePlanRepository.findByPlantId("")).thenReturn(Optional.of(planIdVide));
        when(carePlanRepository.save(any(CarePlan.class))).thenAnswer(inv -> inv.getArgument(0));

        carePlanService.addTaskToPlan("", "task-empty");

        assertTrue(planIdVide.getTaskIds().contains("task-empty"));
        verify(carePlanRepository, times(1)).save(planIdVide);
    }
}