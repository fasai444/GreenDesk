package org.example.controllers.care;

import jakarta.validation.Valid;
import org.example.dto.care.CareTaskResponseDto;
import org.example.dto.care.CreateManualTaskRequest;
import org.example.dto.care.GenerateTasksRequest;
import org.example.entities.care.CareTask;
import org.example.services.scheduling.CareTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * CARE TASK CONTROLLER
 * ============================================================
 * * API REST des tâches de soins sécurisée par DTO.
 * * ============================================================
 */
@RestController
@RequestMapping("/api/care-tasks")
public class CareTaskController {

    private final CareTaskService careTaskService;

    public CareTaskController(CareTaskService careTaskService) {
        this.careTaskService = careTaskService;
    }

    /**
     * ============================================================
     * Toutes les tâches
     * ============================================================
     */
    @GetMapping
    public ResponseEntity<List<CareTaskResponseDto>> getAllTasks() {
        List<CareTask> tasks = careTaskService.getAllTasks();

        List<CareTaskResponseDto> dtos = tasks.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * ============================================================
     * Génération manuelle (Mise à jour DTO)
     * ============================================================
     * * Désormais, on ne passe plus l'entité 'Plant' brute.
     * L'orchestration finale ira chercher la plante via le plantId.
     */
    @PostMapping("/generate")
    public ResponseEntity<List<CareTaskResponseDto>> generateTasks(
            @Valid @RequestBody GenerateTasksRequest request
    ) {
        // Adaptation temporaire du service : on génère la tâche basée sur le payload reçu
        // Note : On retourne une liste car le CDC parle d'un recompute/generate global ou ciblé
        List<CareTask> generatedTasks = careTaskService.generateTasksForRequest(request);

        List<CareTaskResponseDto> dtos = generatedTasks.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    /**
     * ============================================================
     * DONE
     * ============================================================
     */
    @PostMapping("/{id}/done")
    public ResponseEntity<CareTaskResponseDto> markDone(
            @PathVariable String id
    ) {
        CareTask updatedTask = careTaskService.markAsDone(id);
        return ResponseEntity.ok(convertToResponseDto(updatedTask));
    }

    /**
     * ============================================================
     * CANCEL
     * ============================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(
            @PathVariable String id
    ) {
        careTaskService.cancelTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ============================================================
     * Méthode de Mapping Manuelle (Entité -> DTO)
     * ============================================================
     */
    private CareTaskResponseDto convertToResponseDto(CareTask task) {
        if (task == null) {
            return null;
        }

        CareTaskResponseDto dto = new CareTaskResponseDto();
        dto.setId(task.getId());
        dto.setPlantId(task.getPlantId());
        dto.setForestId(task.getForestId());
        dto.setType(task.getType());
        dto.setDescription(task.getDescription());
        dto.setWnsScore(task.getWnsScore());
        dto.setPriority(task.getPriority());
        dto.setFlexible(task.isFlexible());
        dto.setScheduledAt(task.getScheduledAt());
        dto.setDueAt(task.getDueAt());
        dto.setStatus(task.getStatus());
        dto.setWeatherDependency(task.getWeatherDependency());
        dto.setExternalId(task.getExternalId());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setClosedAt(task.getClosedAt());

        // Construction du wnsBreakdown requis par le cahier des charges (6.4)
        // La structure pourra être enrichie dynamiquement par la suite par la Personne B
        dto.setWnsBreakdown(Collections.singletonMap("globalScore", task.getWnsScore()));

        // Note: Le plantName sera résolu plus tard (via jointure ou appel à PlantService)
        dto.setPlantName("Plante #" + task.getPlantId());

        return dto;
    }

    @PostMapping("/manual")
    public ResponseEntity<CareTaskResponseDto> createManualTask(
            @RequestBody CreateManualTaskRequest request
    ) {

        CareTask task = careTaskService.createManualTask(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponseDto(task));
    }
}