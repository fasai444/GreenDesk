package org.example.controllers.forest;

import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.services.ForestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/forests")
public class ForestController {
    @Autowired
    private ForestService forestService;

    /**
     * POST /api/forests
     * Crée une nouvelle forêt.
     */
    @PostMapping
    public ResponseEntity<?> createForest(@RequestBody Map<String, Object> request) {
        try {
            String name = requireString(request, "name");
            int width = requireInt(request, "width");
            int height = requireInt(request, "height");

            Forest forest = forestService.createForest(name, width, height);
            return ResponseEntity.status(HttpStatus.CREATED).body(forest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/forests
     * Récupère toutes les forêts.
     */
    @GetMapping
    public ResponseEntity<List<Forest>> getAllForests() {
        List<Forest> forests = forestService.getAllForests();
        return ResponseEntity.ok(forests);
    }

    /**
     * GET /api/forests/{forestId}
     * Récupère une forêt par son ID.
     */
    @GetMapping("/{forestId}")
    public ResponseEntity<?> getForestById(@PathVariable String forestId) {
        return forestService.getForestById(forestId)
                .map(forest -> ResponseEntity.ok((Object) forest))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Forêt introuvable")));
    }

    /**
     * POST /api/forests/{forestId}/plants
     * Ajoute une plante à une forêt à une position (x, y).
     *
     * Body: { "plantId": "...", "x": 3, "y": 5 }
     *
     * R1 : Vérifie l'unicité de position → 409 si occupée
     * R2 : Vérifie la diversité → 409 si clone
     */
    @PostMapping("/{forestId}/plants")
    public ResponseEntity<?> addPlantToForest(
            @PathVariable String forestId,
            @RequestBody Map<String, Object> request) {
        try {
            String plantId = requireString(request, "plantId");
            int x = requireInt(request, "x");
            int y = requireInt(request, "y");

            Forest forest = forestService.addPlantToForest(forestId, plantId, x, y);
            return ResponseEntity.status(HttpStatus.CREATED).body(forest);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Champ invalide")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", e.getMessage()));
            }

            // R1 ou R2 violation : position occupée ou clone
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/forests/{forestId}/plants
     * Récupère toutes les plantes d'une forêt.
     */
    @GetMapping("/{forestId}/plants")
    public ResponseEntity<List<Plant>> getPlantsInForest(@PathVariable String forestId) {
        List<Plant> plants = forestService.getPlantsInForest(forestId);
        return ResponseEntity.ok(plants);
    }

    /**
     * DELETE /api/forests/{forestId}/plants
     * Retire une plante d'une position dans la forêt.
     *
     * Query params: ?x=3&y=5
     */
    @DeleteMapping("/{forestId}/plants")
    public ResponseEntity<?> removePlantFromForest(
            @PathVariable String forestId,
            @RequestParam int x,
            @RequestParam int y) {
        try {
            Forest forest = forestService.removePlantFromForest(forestId, x, y);
            return ResponseEntity.ok(forest);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/forests/{forestId}
     * Supprime une forêt.
     */
    @DeleteMapping("/{forestId}")
    public ResponseEntity<?> deleteForest(@PathVariable String forestId) {
        try {
            forestService.deleteForest(forestId);
            return ResponseEntity.ok(Map.of("message", "Forêt supprimée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String requireString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (!(value instanceof String str) || str.isBlank()) {
            throw new IllegalArgumentException("Champ invalide: " + key);
        }
        return str;
    }

    private int requireInt(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Number number) {
            int intValue = number.intValue();
            if (!Objects.equals(number.doubleValue(), (double) intValue)) {
                throw new IllegalArgumentException("Champ invalide: " + key);
            }
            return intValue;
        }

        if (value instanceof String stringValue) {
            try {
                return Integer.parseInt(stringValue.trim());
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Champ invalide: " + key);
            }
        }

        throw new IllegalArgumentException("Champ invalide: " + key);
    }
}
