package org.example.controllers.species;

import org.example.entites.species.Species;
import org.example.services.SpeciesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/species") // Tous les endpoints commenceront par /api/species
public class SpeciesController {

    @Autowired
    private SpeciesServices speciesServices;

    // ----------------- GET all species -----------------
    @GetMapping
    public Iterable<Species> getAllSpecies() {
        return speciesServices.getAllSpecies();
    }

    // ----------------- GET species by name -----------------
    @GetMapping("/{name}")
    public ResponseEntity<Species> getSpeciesByName(@PathVariable String name) {
        Optional<Species> speciesOpt = speciesServices.getSpeciesByName(name);
        return speciesOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------- POST create species -----------------
    @PostMapping
    public ResponseEntity<?> createSpecies(@RequestBody Species species) {
        try {
            Species created = speciesServices.createSpecies(species);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------- PUT update species by ID -----------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSpecies(
            @PathVariable String id,
            @RequestBody Species updatedSpecies) {

        try {
            // On supprime l'ancienne espèce
            speciesServices.deleteSpeciesById(id);

            // On recrée avec les nouvelles valeurs
            Species saved = speciesServices.createSpecies(updatedSpecies);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------- DELETE species by ID -----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpecies(@PathVariable String id) {
        try {
            speciesServices.deleteSpeciesById(id);
            return ResponseEntity.ok("Espèce supprimée avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ----------------- DELETE ALL species -----------------
    @DeleteMapping
    public ResponseEntity<?> deleteAllSpecies() {
        speciesServices.deleteAllSpecies();
        return ResponseEntity.ok("Toutes les espèces ont été supprimées");
    }
}
