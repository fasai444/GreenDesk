package org.example.controllers.forest;

import org.example.services.ForestComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forests")
public class ForestComparisonController {

    @Autowired
    private ForestComparisonService forestComparisonService;

    /**
     * GET /api/forests/compare?forestId1=...&forestId2=...
     * Compare deux forêts côte à côte.
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compareForests(
            @RequestParam String forestId1,
            @RequestParam String forestId2) {
        try {
            Map<String, Object> result = forestComparisonService.compareForests(forestId1, forestId2);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
