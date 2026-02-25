package org.example.controllers.alert;

import org.example.entities.alert.PlantAlert;
import org.example.services.PlantAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlantAlertController {

    @Autowired
    private PlantAlertService plantAlertService;

    @GetMapping("/plants/{plantId}/alerts")
    public ResponseEntity<?> listAlerts(@PathVariable String plantId,
                                        @RequestParam(defaultValue = "true") boolean active) {
        try {
            List<PlantAlert> alerts = plantAlertService.getAlertsForPlant(plantId, active);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/alerts/{alertId}/ack")
    public ResponseEntity<?> acknowledge(@PathVariable String alertId) {
        try {
            PlantAlert updated = plantAlertService.acknowledge(alertId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}