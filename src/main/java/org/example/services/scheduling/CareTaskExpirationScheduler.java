package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.TaskStatus;
import org.example.repositories.CareTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * ============================================================
 * CARE TASK EXPIRATION SCHEDULER
 * ============================================================
 *
 * Tâche de fond planifiée chargée de nettoyer et d'annuler
 * automatiquement les tâches PENDING qui ont passé leur dueAt.
 *
 * ============================================================
 */
@Component
public class CareTaskExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(CareTaskExpirationScheduler.class);

    private final CareTaskRepository careTaskRepository;

    public CareTaskExpirationScheduler(CareTaskRepository careTaskRepository) {
        this.careTaskRepository = careTaskRepository;
    }

    /**
     * S'exécute selon la fréquence cron configurée.
     * Nettoie les tâches périmées à chaque passage.
     */
    @Scheduled(cron = "${care.scheduler.expiration.cleanup.cron:0 30 * * * *}")
    public void cleanupExpiredTasks() {
        Instant now = Instant.now();
        log.info("Starting automatic care tasks expiration cleanup at {}", now);

        // Récupération des tâches PENDING dont le dueAt est dans le passé
        List<CareTask> expiredTasks = careTaskRepository.findByStatusAndDueAtBefore(TaskStatus.PENDING, now);

        if (expiredTasks.isEmpty()) {
            log.info("No expired care tasks found.");
            return;
        }

        for (CareTask task : expiredTasks) {
            try {
                task.setStatus(TaskStatus.CANCELED);
                task.setClosedAt(now);
                careTaskRepository.save(task);

                // Log structuré (Critère AC-9 / Section 13.1 du CDC)
                log.info("[CARE_TASK_EXPIRED] {{taskId: '{}', plantId: '{}', reason: 'dueAt {} exceeded now {}'}}",
                        task.getId(), task.getPlantId(), task.getDueAt(), now);

            } catch (Exception e) {
                log.error("Failed to automatically expire care task with ID: {}", task.getId(), e);
            }
        }

        log.info("Cleanup finished. Total tasks expired: {}", expiredTasks.size());
    }
}