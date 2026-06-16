package org.example.controllers.plant;

import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.services.SensorReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/plants/{plantId}/sensor-readings")
public class SensorReadingController {

    @Autowired
    private SensorReadingService sensorReadingService;

    @PostMapping
    public ResponseEntity<?> createReading(@PathVariable String plantId,
                                           @RequestBody CreateSensorReadingRequest request) {
        try {
            SensorReading saved = sensorReadingService.addReading(plantId, request);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listReadings(@PathVariable String plantId,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to) {
        try {
            if (from != null && to != null) {
                LocalDateTime f = LocalDateTime.parse(from);
                LocalDateTime t = LocalDateTime.parse(to);
                List<SensorReading> list = sensorReadingService.getReadingsBetween(plantId, f, t);
                return ResponseEntity.ok(list);
            }
            return ResponseEntity.ok(sensorReadingService.getReadings(plantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> latest(@PathVariable String plantId) {
        try {
            return ResponseEntity.ok(sensorReadingService.getLatest(plantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}