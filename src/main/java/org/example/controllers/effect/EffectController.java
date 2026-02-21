package org.example.controllers.effect;

import org.example.entites.effect.Effect;
import org.example.entites.plant.PlantEffect;
import org.example.services.EffectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api") // Gardé /api pour être cohérent avec vos autres contrôleurs
@CrossOrigin(origins = "*") // Permet au frontend de communiquer avec l'API
public class EffectController {
    
    @Autowired
    private EffectService effectService;
    
    /**
     * GET /api/effects
     * Récupère le catalogue de tous les effets disponibles (L2-F2).
     * Supporte le filtre ?custom=true pour la L3-F1.
     */
    @GetMapping("/effects") // Ajout du path spécifique ici
    public ResponseEntity<List<Effect>> getAllEffects(@RequestParam(required = false) Boolean custom) {
        
        // Initialiser le catalogue s'il n'existe pas
        effectService.initializeEffectsCatalog();
        
        // Appel au service avec le paramètre de filtrage 
        List<Effect> effects = effectService.getAllEffects();
        
        return ResponseEntity.ok(effects);
    }

    /**
     * POST /api/effects
     * Créer un effet personnalisé (L3-F1). [cite: 133]
     */
    @PostMapping("/effects")
    public ResponseEntity<Effect> createCustomEffect(@RequestBody Effect effect) {
        // Appelle la nouvelle méthode du service pour forcer isCustom=true [cite: 131]
        Effect createdEffect = effectService.createCustomEffect(effect);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEffect);
    }
    
    /**
     * POST /api/plants/{plantId}/effects/{effectId}
     * Applique un effet à une plante (L2-F2). [cite: 102]
     */
    @PostMapping("/plants/{plantId}/effects/{effectId}")
    public ResponseEntity<?> applyEffectToPlant(
            @PathVariable String plantId,
            @PathVariable String effectId) {
        try {
            PlantEffect plantEffect = effectService.applyEffectToPlant(plantId, effectId);
            return ResponseEntity.status(HttpStatus.CREATED).body(plantEffect);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/plants/{plantId}/effects
     * Récupère tous les effets d'une plante (L2-F2). [cite: 103]
     */
    @GetMapping("/plants/{plantId}/effects")
    public ResponseEntity<List<PlantEffect>> getPlantEffects(@PathVariable String plantId) {
        List<PlantEffect> effects = effectService.getPlantEffects(plantId);
        return ResponseEntity.ok(effects);
    }
    
    /**
     * GET /api/plants/{plantId}/effects/active
     * Récupère les effets actifs d'une plante.
     */
    @GetMapping("/plants/{plantId}/effects/active")
    public ResponseEntity<List<PlantEffect>> getActivePlantEffects(@PathVariable String plantId) {
        List<PlantEffect> effects = effectService.getActivePlantEffects(plantId);
        return ResponseEntity.ok(effects);
    }
    
    /**
     * DELETE /api/plants/effects/{plantEffectId}
     * Retire (désactive) un effet d'une plante (L2-F2). [cite: 95]
     */
    @DeleteMapping("/plants/effects/{plantEffectId}")
    public ResponseEntity<?> removeEffectFromPlant(@PathVariable String plantEffectId) {
        try {
            effectService.removeEffectFromPlant(plantEffectId);
            return ResponseEntity.ok(Map.of("message", "Effet retiré avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}