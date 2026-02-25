package org.example.controllers.stimulus;

import org.example.entities.Stimulus;
import org.example.services.StimulusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stimuli")
public class StimulusController {

    @Autowired
    private StimulusService stimulusService;

    @PostMapping
    public ResponseEntity<?> createStimulus(@RequestBody Stimulus stimulus) {
        try {
            Stimulus saved = stimulusService.applyToForest(stimulus);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
