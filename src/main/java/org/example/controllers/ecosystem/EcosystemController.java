package org.example.controllers.ecosystem;

import org.example.entities.ecosystem.Ecosystem;
import org.example.entities.ecosystem.diseases.BacterialDisease;
import org.example.entities.ecosystem.diseases.MildiouDisease;
import org.example.entities.ecosystem.diseases.RustDisease;
import org.example.entities.ecosystem.EcosystemCell;
import org.example.entities.ecosystem.diseases.PlantDisease;
import org.example.entities.plant.Plant;
import org.example.entities.forest.Forest;
import org.example.repositories.ForestRepository;
import org.example.services.EcosystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ecosystem")
public class EcosystemController {

    private final EcosystemService ecosystemService;
    private final ForestRepository forestRepository;

    // On garde un écosystème en mémoire pour la session
    private Ecosystem ecosystem;

    public EcosystemController(EcosystemService ecosystemService,
            ForestRepository forestRepository) {
        this.ecosystemService = ecosystemService;
        this.forestRepository = forestRepository;
    }

    /**
     * Initialise un écosystème à partir de la première forêt en BDD.
     */
    private void ensureEcosystemInitialized() {
        if (ecosystem != null)
            return; // déjà initialisé

        // Cherche la première forêt existante
        Forest forest = forestRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Aucune forêt trouvée en base"));

        ensureForestCellsInitialized(forest);
        ecosystem = new Ecosystem(forest);
        ecosystem.initialiseEcosystem();

        // Injecte l'écosystème dans le service
        ecosystemService.setEcosystem(ecosystem);
    }

    private void ensureForestCellsInitialized(Forest forest) {
        for (Plant plant : forest.getPlants()) {
            boolean alreadyAssigned = forest.getCells().stream()
                    .anyMatch(c -> plant.getId().equals(c.getPlantId()));
            if (alreadyAssigned) {
                continue;
            }

            Forest.ForestCell freeCell = findFirstFreeCell(forest);
            if (freeCell == null) {
                break;
            }

            forest.addCell(new Forest.ForestCell(freeCell.getX(), freeCell.getY(), plant.getId()));
        }
    }

    private Forest.ForestCell findFirstFreeCell(Forest forest) {
        for (int y = 0; y < forest.getHeight(); y++) {
            for (int x = 0; x < forest.getWidth(); x++) {
                if (!forest.isPositionOccupied(x, y)) {
                    return new Forest.ForestCell(x, y, null);
                }
            }
        }

        return null;
    }

    // Créer un écosystème avec une forêt donnée
    private void ensureEcosystemInitialized(String forestId) {
        if (ecosystem != null && ecosystem.getForest().getId().equals(forestId))
            return;

        Forest forest = forestRepository.findById(Objects.requireNonNull(forestId))
                .orElseThrow(() -> new IllegalStateException("Aucune forêt trouvée avec l'ID : " + forestId));

        initEcosystemWithForest(forest);
    }

    private void initEcosystemWithForest(Forest forest) {
        // Associer les plantes à des cellules si nécessaire
        for (Plant plant : forest.getPlants()) {
            boolean alreadyAssigned = forest.getCells().stream()
                    .anyMatch(c -> plant.getId().equals(c.getPlantId()));
            if (alreadyAssigned) {
                continue;
            }

            Integer plantX = plant.getX();
            Integer plantY = plant.getY();
            int x = plantX != null ? plantX : -1;
            int y = plantY != null ? plantY : -1;
            boolean hasValidCoordinates = x >= 0 && y >= 0 && x < forest.getWidth() && y < forest.getHeight();

            if (!hasValidCoordinates || forest.isPositionOccupied(x, y)) {
                Forest.ForestCell freeCell = findFirstFreeCell(forest);
                if (freeCell == null) {
                    break;
                }
                x = freeCell.getX();
                y = freeCell.getY();
            }

            plant.setX(x);
            plant.setY(y);
            forest.addCell(new Forest.ForestCell(x, y, plant.getId()));
        }

        ecosystem = new Ecosystem(forest);
        ecosystem.initialiseEcosystem();
        ecosystemService.setEcosystem(ecosystem);

        int nbInitialDiseased = Math.min(15, forest.getPlants().size());
        int infected = 0;
        while (infected < nbInitialDiseased) {
            EcosystemCell cell = ecosystem.getCells().get((int) (Math.random() * ecosystem.getCells().size()));
            if (!cell.hasPlant() || cell.isDiseased())
                continue;

            double severity = 0.05 + Math.random() * 0.05;

            PlantDisease disease = switch ((int) (Math.random() * 3)) {
                case 0 -> new BacterialDisease(severity);
                case 1 -> new MildiouDisease(severity);
                default -> new RustDisease(severity);
            };
            // Infecte la cellule via le service
            cell.infect(disease);
            infected++;
        }
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
        System.out.println("Nombre de cellules dans l'écosystème : " + ecosystemService.getEcosystemCells().size());
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

    @PostMapping("/simulate/{forestId}/{n}")
    public String simulateTicks(@PathVariable String forestId, @PathVariable int n) {
        ensureEcosystemInitialized(forestId);
        ecosystemService.simulateTicks(n);
        return n + " ticks simulés pour la forêt " + forestId + ".\n" + ecosystemService.getEcosystemStatus();
    }

    @GetMapping("/cells/{forestId}")
    public List<String> getCellsStatus(@PathVariable String forestId) {
        ensureEcosystemInitialized(forestId);
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

    // Tick pour forêt spécifique
    @PostMapping("/tick/{forestId}")
    public String tick(@PathVariable String forestId) {
        ensureEcosystemInitialized(forestId);
        ecosystemService.tick();
        return "Tick effectué pour la forêt " + forestId + ".\n" + ecosystemService.getEcosystemStatus();
    }
}