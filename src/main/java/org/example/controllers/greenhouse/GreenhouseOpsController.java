package org.example.controllers.greenhouse;

import org.example.services.GreenhouseOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/greenhouse")
public class GreenhouseOpsController {

    @Autowired
    private GreenhouseOpsService greenhouseOpsService;

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {
        try {
            return ResponseEntity.ok(greenhouseOpsService.getOverview());
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/overview");
        }
    }

    @GetMapping("/live-effects")
    public ResponseEntity<?> getLiveEffects(@RequestParam(defaultValue = "20") int limit) {
        try {
            int safeLimit = normalize(limit, 1, 500);
            return ResponseEntity.ok(greenhouseOpsService.getLiveEffectsImpact(safeLimit));
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/live-effects");
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getAlerts(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            int safeHours = normalize(hours, 1, 720);
            int safeLimit = normalize(limit, 1, 500);
            return ResponseEntity.ok(greenhouseOpsService.getActiveAlerts(safeHours, safeLimit));
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/alerts");
        }
    }

    @GetMapping("/roi")
    public ResponseEntity<?> getRoi(@RequestParam(defaultValue = "24") int hours) {
        try {
            int safeHours = normalize(hours, 1, 720);
            return ResponseEntity.ok(greenhouseOpsService.getRoiInsights(safeHours));
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/roi");
        }
    }

    @GetMapping("/roi/forests")
    public ResponseEntity<?> getForestRoi(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            int safeLimit = normalize(limit, 1, 500);
            int safeHours = normalize(hours, 1, 720);
            return ResponseEntity.ok(greenhouseOpsService.getForestRoiRanking(safeLimit, safeHours));
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/roi/forests");
        }
    }

    @PostMapping("/sensor-stream/tick")
    public ResponseEntity<?> emitSensorTick(@RequestBody GreenhouseTickRequest request) {
        try {
            String forestId = request == null || request.forestId() == null ? "" : request.forestId().trim();
            if (forestId.isBlank()) {
                return badRequest("forestId est requis", "/sensor-stream/tick");
            }
            String normalizedProfile = normalizeProfile(request == null ? null : request.profile());
            return ResponseEntity.ok(greenhouseOpsService.emitSensorTick(forestId, normalizedProfile));
        } catch (Exception e) {
            return badRequest(e.getMessage(), "/sensor-stream/tick");
        }
    }

    private int normalize(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    private String normalizeProfile(String profile) {
        if (profile == null || profile.isBlank()) {
            return "NORMAL";
        }
        return profile.trim().toUpperCase();
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message, String endpoint) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", message == null || message.isBlank() ? "Bad request" : message,
                "endpoint", endpoint,
                "timestamp", LocalDateTime.now().toString()));
    }

    public record GreenhouseTickRequest(String forestId, String profile) {
    }
}
