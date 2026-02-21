package org.example.controllers.ecosystem;

import org.example.entites.ecosystem.Ecosystem;
import org.example.entites.forest.Forest;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.services.EcosystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ecosystem")
public class EcosystemController {

    private final EcosystemService ecosystemService;
    private final ForestRepository forestRepository;
    private final PlantRepository plantRepository;

    // On garde un écosystème en mémoire pour la session
    private Ecosystem ecosystem;

    public EcosystemController(EcosystemService ecosystemService,
                               ForestRepository forestRepository,
                               PlantRepository plantRepository) {
        this.ecosystemService = ecosystemService;
        this.forestRepository = forestRepository;
        this.plantRepository = plantRepository;
    }

    /**
     * Initialise un écosystème à partir de la première forêt en BDD.
     */
    private void ensureEcosystemInitialized() {
        if (ecosystem != null) return; // déjà initialisé

        // Cherche la première forêt existante
        Forest forest = forestRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune forêt trouvée en base"));

        ecosystem = new Ecosystem(forest);
        ecosystem.initialiseEcosystem();

        // Injecte l'écosystème dans le service
        ecosystemService.setEcosystem(ecosystem);
    }

    // Lancer un tick
    @PostMapping("/tick")
    public String tick() {
        ensureEcosystemInitialized();
        ecosystemService.tick();
        return "Tick effectué.\n" + ecosystemService.getEcosystemStatus();
    }

    // Lancer plusieurs ticks
    @PostMapping("/simulate/{n}")
    public String simulateTicks(@PathVariable int n) {
        ensureEcosystemInitialized();
        ecosystemService.simulateTicks(n);
        return n + " ticks simulés.\n" + ecosystemService.getEcosystemStatus();
    }

    // Voir l’état détaillé de chaque cellule
    @GetMapping("/cells")
    public List<String> getCellsStatus() {
        ensureEcosystemInitialized();
        return ecosystemService.getEcosystemCells().stream()
                .map(cell -> {
                    String plantId = cell.getForestCell().getPlantId();
                    String disease = cell.isDiseased() ? cell.getDisease().getName() : "Healthy";
                    double severity = cell.isDiseased() ? cell.getDisease().getSeverity() : 0.0;
                    return "Cell [" + cell.getForestCell().getX() + "," + cell.getForestCell().getY() +
                            "] Plant: " + plantId +
                            " | Disease: " + disease +
                            " | Severity: " + severity;
                })
                .collect(Collectors.toList());
    }
}