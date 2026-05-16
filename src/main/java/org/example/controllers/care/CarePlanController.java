package org.example.controllers.care;

import org.example.entities.care.CarePlan;
import org.example.entities.care.CareTask;
import org.example.repositories.CareTaskRepository;
import org.example.services.scheduling.CarePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/care-plan")
public class CarePlanController {

    private final CarePlanService carePlanService;
    private final CareTaskRepository careTaskRepository;

    public CarePlanController(CarePlanService carePlanService, CareTaskRepository careTaskRepository) {
        this.carePlanService = carePlanService;
        this.careTaskRepository = careTaskRepository;
    }

    @GetMapping("/{plantId}")
    public ResponseEntity<Map<String, Object>> getPlanByPlant(@PathVariable String plantId) {
        CarePlan plan = carePlanService.getOrCreatePlan(plantId);
        List<CareTask> tasks = careTaskRepository.findAllById(plan.getTaskIds());

        Map<String, Object> response = new HashMap<>();
        response.put("id", plan.getId());
        response.put("plantId", plan.getPlantId());
        response.put("lastRecalculationDate", plan.getLastRecalculationDate());
        response.put("tasks", tasks);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/recompute")
    public ResponseEntity<Void> recomputePlan(@RequestBody Map<String, String> requestBody) {
        String forestId = requestBody.get("forestId");
        String plantId = requestBody.get("plantId");

        carePlanService.recomputeGlobalPlan(forestId, plantId);
        return ResponseEntity.ok().build();
    }

}