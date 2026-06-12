package org.example.controllers.care;

import jakarta.validation.Valid;
import org.example.dto.care.CareTaskResponseDto;
import org.example.dto.care.CareTaskUpdateRequest;
import org.example.dto.care.CreateManualTaskRequest;
import org.example.dto.care.CreateTaskRequest;
import org.example.dto.care.GenerateTasksRequest;
import org.example.entities.care.CareTask;
import org.example.repositories.PlantRepository;
import org.example.services.scheduling.CareTaskService;
import org.example.services.scheduling.WnsCalculator;
import org.example.services.scheduling.WnsResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/care-tasks")
public class CareTaskController {

    private final CareTaskService careTaskService;
    private final PlantRepository plantRepository;
    private final WnsCalculator wnsCalculator;

    public CareTaskController(CareTaskService careTaskService,
                              PlantRepository plantRepository,
                              WnsCalculator wnsCalculator) {
        this.careTaskService = careTaskService;
        this.plantRepository = plantRepository;
        this.wnsCalculator = wnsCalculator;
    }

    @GetMapping
    public ResponseEntity<List<CareTaskResponseDto>> getAllTasks() {
        List<CareTaskResponseDto> dtos = careTaskService.getAllTasks().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * POST tâches : création automatique via le moteur WNS + push agenda Google.
     */
    @PostMapping
    public ResponseEntity<CareTaskResponseDto> createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        CareTask task = careTaskService.createTaskForPlant(request.getPlantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(task));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<CareTaskResponseDto>> generateTasks(
            @Valid @RequestBody GenerateTasksRequest request
    ) {
        List<CareTaskResponseDto> dtos = careTaskService.generateTasksForRequest(request).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    /**
     * PATCH tâches flexibles : déplacement manuel ou suite à une alerte météo.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CareTaskResponseDto> patchTask(
            @PathVariable String id,
            @Valid @RequestBody CareTaskUpdateRequest request
    ) {
        CareTask updated = careTaskService.patchFlexibleTask(id, request);
        return ResponseEntity.ok(convertToResponseDto(updated));
    }

    /**
     * PUT tâche validée : clôture + mise à jour santé de la plante.
     */
    @PutMapping("/{id}/validate")
    public ResponseEntity<CareTaskResponseDto> validateTask(@PathVariable String id) {
        CareTask updated = careTaskService.validateTask(id);
        return ResponseEntity.ok(convertToResponseDto(updated));
    }

    @PostMapping("/{id}/done")
    public ResponseEntity<CareTaskResponseDto> markDone(@PathVariable String id) {
        CareTask updated = careTaskService.markAsDone(id);
        return ResponseEntity.ok(convertToResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(@PathVariable String id) {
        careTaskService.cancelTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual")
    public ResponseEntity<CareTaskResponseDto> createManualTask(
            @Valid @RequestBody CreateManualTaskRequest request
    ) {
        CareTask task = careTaskService.createManualTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponseDto(task));
    }

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

        plantRepository.findById(task.getPlantId()).ifPresent(plant -> {
            dto.setPlantName(plant.getName());
            WnsResult wns = wnsCalculator.calculate(plant, List.of());
            dto.setWnsBreakdown(wns.getBreakdown());
        });

        if (dto.getWnsBreakdown() == null) {
            dto.setWnsBreakdown(java.util.Map.of("globalScore", task.getWnsScore()));
        }

        if (dto.getPlantName() == null) {
            dto.setPlantName("Plante #" + task.getPlantId());
        }

        return dto;
    }
}
