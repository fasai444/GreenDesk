package org.example.repositories;

import org.example.entities.care.CareTask;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskPriority;
import org.example.entities.care.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * CARE TASK REPOSITORY
 * ============================================================
 *
 * Repository MongoDB des tâches de soins.
 *
 * Fournit :
 * - filtres dashboard,
 * - support scheduler,
 * - idempotence,
 * - workflows métier.
 *
 * ============================================================
 */

public interface CareTaskRepository
        extends MongoRepository<CareTask, String> {

    /**
     * Toutes les tâches d’une plante
     */
    List<CareTask> findByPlantId(String plantId);

    /**
     * Toutes les tâches d’une forêt
     */
    List<CareTask> findByForestId(String forestId);

    /**
     * Filtre par statut
     */
    List<CareTask> findByStatus(TaskStatus status);

    /**
     * Filtre par priorité
     */
    List<CareTask> findByPriority(TaskPriority priority);

    /**
     * Recherche métier scheduler :
     * tâches expirées encore PENDING
     */
    List<CareTask> findByStatusAndDueAtBefore(
            TaskStatus status,
            Instant now
    );

    /**
     * Vérifie si une tâche similaire existe déjà
     * (idempotence)
     */
    Optional<CareTask> findByPlantIdAndTypeAndScheduledAt(
            String plantId,
            CareTaskType type,
            Instant scheduledAt
    );

    /**
     * Toutes les tâches actives d’une plante
     */
    List<CareTask> findByPlantIdAndStatus(
            String plantId,
            TaskStatus status
    );

    /**
     * Dashboard principal :
     * tri priorité + échéance
     */
    List<CareTask>
    findAllByOrderByPriorityDescDueAtAsc();
    /**
     * Recherche une tâche spécifique pour une plante qui n'a pas encore été traitée.
     * Utilisé comme verrou d'idempotence pour éviter les doublons dans Google Calendar.
     */
    Optional<CareTask> findByPlantIdAndTypeAndStatus(String plantId, CareTaskType type, TaskStatus status);
}