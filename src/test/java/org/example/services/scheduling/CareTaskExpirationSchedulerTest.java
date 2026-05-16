package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.repositories.CareTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class CareTaskExpirationSchedulerTest {

    @Mock
    private CareTaskRepository careTaskRepository;

    @InjectMocks
    private CareTaskExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("cleanupExpiredTasks : Ne fait rien si aucune tâche n'est expirée")
    void shouldDoNothingWhenNoExpiredTasksFound() {
        // Arrange
        when(careTaskRepository.findByStatusAndDueAtBefore(eq(TaskStatus.PENDING), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.cleanupExpiredTasks();

        // Assert
        verify(careTaskRepository, never()).save(any());
    }

    @Test
    @DisplayName("cleanupExpiredTasks : Passe toutes les tâches périmées au statut CANCELED")
    void shouldExpireAllTasksWhenFound() {
        // Arrange
        CareTask task1 = new CareTask();
        ReflectionTestUtils.setField(task1, "id", "task-1");
        task1.setStatus(TaskStatus.PENDING);

        CareTask task2 = new CareTask();
        ReflectionTestUtils.setField(task2, "id", "task-2");
        task2.setStatus(TaskStatus.PENDING);

        when(careTaskRepository.findByStatusAndDueAtBefore(eq(TaskStatus.PENDING), any()))
                .thenReturn(List.of(task1, task2));

        // Act
        scheduler.cleanupExpiredTasks();

        // Assert
        verify(careTaskRepository, times(1)).save(task1);
        verify(careTaskRepository, times(1)).save(task2);
    }

    @Test
    @DisplayName("cleanupExpiredTasks : Doit continuer le traitement des autres tâches même si une sauvegarde plante (Robustesse)")
    void shouldContinueProcessingEvenIfOneTaskFailsToSave() {
        // Arrange
        CareTask faultyTask = new CareTask();
        ReflectionTestUtils.setField(faultyTask, "id", "faulty-task");
        faultyTask.setStatus(TaskStatus.PENDING);

        CareTask robustTask = new CareTask();
        ReflectionTestUtils.setField(robustTask, "id", "robust-task");
        robustTask.setStatus(TaskStatus.PENDING);

        when(careTaskRepository.findByStatusAndDueAtBefore(eq(TaskStatus.PENDING), any()))
                .thenReturn(List.of(faultyTask, robustTask));

        // On simule une levée d'exception uniquement sur la première tâche
        when(careTaskRepository.save(faultyTask)).thenThrow(new RuntimeException("MongoDB Disconnect simulation"));
        when(careTaskRepository.save(robustTask)).thenReturn(robustTask);

        // Act
        scheduler.cleanupExpiredTasks();

        // Assert
        verify(careTaskRepository, times(1)).save(faultyTask);
        verify(careTaskRepository, times(1)).save(robustTask); // Doit quand même être appelée !
    }
}