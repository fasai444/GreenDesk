package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;
import org.example.entities.care.TaskStatus;
import org.example.entities.plant.GrowthStage;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.CareTaskRepository;
import org.example.repositories.PlantImpactRepository;
import org.example.repositories.PlantRepository;
import org.example.services.calendar.ExternalCalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CareTaskServiceTest {

    @Mock
    private CareTaskRepository careTaskRepository;

    @Mock
    private ExternalCalendarService externalCalendarService;

    @Mock
    private WnsCalculator wnsCalculator;

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private PlantImpactRepository plantImpactRepository;

    @Mock
    private CarePlanService carePlanService;

    @InjectMocks
    private CareTaskService careTaskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(plantImpactRepository.findByPlantIdOrderByTimestampDesc(anyString()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Génération : WNS > 0.8 doit générer une tâche WATERING")
    void shouldGenerateWateringTaskWhenWnsIsHigh() {
        Plant plant = createMockPlant("plant-1", "Chêne", 0.85);
        plant.setHeightCm(200);
        plant.setGrowthStage(GrowthStage.VEGETATIVE);
        Species species = new Species("Oak", 5000, 20, 60, 5000, 1, 0.5);
        species.setMaxHeight(300);
        plant.setSpecies(species);
        plant.setWaterLevel(100);

        when(wnsCalculator.calculate(eq(plant), any())).thenReturn(
                new WnsResult(0.92, Map.of("globalScore", 0.92), false, false)
        );
        when(careTaskRepository.findByPlantIdAndTypeAndStatus(anyString(), any(), eq(TaskStatus.PENDING)))
                .thenReturn(Optional.empty());
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
        assertTrue(result.isFlexible());
        assertEquals(0.92, result.getWnsScore(), 0.001);
        assertTrue(result.getDescription().contains("Arrosage"));
        verify(carePlanService).addTaskToPlan("plant-1", "task-water-1");
    }

    @Test
    @DisplayName("Génération : WNS <= 0.8 doit retourner null")
    void shouldReturnNullWhenWnsBelowThreshold() {
        Plant plant = createMockPlant("plant-4", "Fougère", 0.3);

        when(wnsCalculator.calculate(eq(plant), any())).thenReturn(
                new WnsResult(0.45, Map.of("globalScore", 0.45), false, false)
        );

        CareTask result = careTaskService.generateTask(plant);

        assertNull(result);
        verifyNoInteractions(careTaskRepository);
        verifyNoInteractions(externalCalendarService);
    }

    @Test
    @DisplayName("Génération : pluie dans 6h bloque l'arrosage")
    void shouldSkipWateringWhenRainWithin6Hours() {
        Plant plant = createMockPlant("plant-rain", "Tomate", 0.9);
        plant.setWaterLevel(50);
        Species species = new Species("Tomato", 3000, 22, 70, 8000, 1, 0.5);
        plant.setSpecies(species);

        when(wnsCalculator.calculate(eq(plant), any())).thenReturn(
                new WnsResult(0.95, Map.of("globalScore", 0.95), true, true)
        );

        CareTask result = careTaskService.generateTask(plant);

        assertNull(result);
        verifyNoInteractions(careTaskRepository);
    }

    @Test
    @DisplayName("Idempotence : retourne la tâche existante sans doublon")
    void shouldReturnExistingTaskToEnsureIdempotence() {
        Plant plant = createMockPlant("plant-1", "Monstera", 0.9);

        when(wnsCalculator.calculate(eq(plant), any())).thenReturn(
                new WnsResult(0.95, Map.of("globalScore", 0.95), false, false)
        );

        CareTask existingTask = new CareTask();
        ReflectionTestUtils.setField(existingTask, "id", "existing-unique-id");
        existingTask.setStatus(TaskStatus.PENDING);

        when(careTaskRepository.findByPlantIdAndTypeAndStatus(anyString(), any(), eq(TaskStatus.PENDING)))
                .thenReturn(Optional.of(existingTask));

        CareTask result = careTaskService.generateTask(plant);

        assertNotNull(result);
        assertEquals("existing-unique-id", result.getId());
        verify(careTaskRepository, never()).save(any(CareTask.class));
        verifyNoInteractions(externalCalendarService);
    }

    @Test
    @DisplayName("Validation : clôture la tâche et met à jour la santé de la plante")
    void shouldValidateTaskAndUpdatePlantHealth() {
        CareTask pendingTask = new CareTask();
        ReflectionTestUtils.setField(pendingTask, "id", "task-done");
        pendingTask.setPlantId("plant-1");
        pendingTask.setType(CareTaskType.WATERING);
        pendingTask.setStatus(TaskStatus.PENDING);

        Plant plant = createMockPlant("plant-1", "Chêne", 0.7);
        Species species = new Species("Oak", 5000, 20, 60, 5000, 1, 0.5);
        plant.setSpecies(species);
        plant.setWaterLevel(100);
        plant.setTemperature(20);
        plant.setHumidity(60);
        plant.setLux(5000);

        when(careTaskRepository.findById("task-done")).thenReturn(Optional.of(pendingTask));
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(plantRepository.findById("plant-1")).thenReturn(Optional.of(plant));
        when(plantRepository.save(any(Plant.class))).thenAnswer(inv -> inv.getArgument(0));

        CareTask result = careTaskService.validateTask("task-done");

        assertEquals(TaskStatus.DONE, result.getStatus());
        assertNotNull(result.getClosedAt());
        verify(plantRepository).save(plant);
        assertTrue(plant.getWaterLevel() > 100);
        assertNotNull(plant.getPlantState());
    }

    @Test
    @DisplayName("Annulation : passe au statut CANCELED et nettoie l'agenda externe")
    void shouldCancelTaskAndRemoveFromExternalCalendar() {
        CareTask task = new CareTask();
        ReflectionTestUtils.setField(task, "id", "task-cancel");
        task.setStatus(TaskStatus.PENDING);
        task.setExternalId("google-event-xyz");

        when(careTaskRepository.findById("task-cancel")).thenReturn(Optional.of(task));
        when(careTaskRepository.save(any(CareTask.class))).thenAnswer(inv -> inv.getArgument(0));

        careTaskService.cancelTask("task-cancel");

        assertEquals(TaskStatus.CANCELED, task.getStatus());
        verify(externalCalendarService).remove("google-event-xyz");
    }

    @Test
    @DisplayName("Validation : une tâche annulée ne peut pas être validée")
    void shouldRejectValidationOfCanceledTask() {
        CareTask canceledTask = new CareTask();
        canceledTask.setStatus(TaskStatus.CANCELED);
        when(careTaskRepository.findById("task-canceled")).thenReturn(Optional.of(canceledTask));

        assertThrows(IllegalStateException.class, () -> careTaskService.validateTask("task-canceled"));

        verify(careTaskRepository, never()).save(any());
        verifyNoInteractions(plantRepository);
    }

    @Test
    @DisplayName("Déplacement : refuse une échéance antérieure à la planification")
    void shouldRejectDueDateBeforeScheduledDate() {
        CareTask task = new CareTask();
        task.setStatus(TaskStatus.PENDING);
        task.setFlexible(true);
        when(careTaskRepository.findById("task-invalid-dates")).thenReturn(Optional.of(task));

        org.example.dto.care.CareTaskUpdateRequest request = new org.example.dto.care.CareTaskUpdateRequest();
        request.setScheduledAt(Instant.now().plusSeconds(7200));
        request.setDueAt(Instant.now().plusSeconds(3600));

        assertThrows(IllegalArgumentException.class,
                () -> careTaskService.patchFlexibleTask("task-invalid-dates", request));

        verify(careTaskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dashboard : récupération globale ordonnée")
    void shouldReturnAllTasksOrderedByPriorityAndDueAt() {
        List<CareTask> mockList = List.of(new CareTask(), new CareTask());
        when(careTaskRepository.findAllByOrderByPriorityDescDueAtAsc()).thenReturn(mockList);

        List<CareTask> result = careTaskService.getAllTasks();

        assertEquals(2, result.size());
        verify(careTaskRepository).findAllByOrderByPriorityDescDueAtAsc();
    }

    private Plant createMockPlant(String id, String name, double stressIndex) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        plant.setStressIndex(stressIndex);
        plant.setForestId("forest-generic-id");
        plant.setGrowthStage(GrowthStage.VEGETATIVE);
        return plant;
    }
}
