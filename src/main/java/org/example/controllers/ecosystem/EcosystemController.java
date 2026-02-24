package org.example.controllers.ecosystem;

import org.example.entites.ecosystem.Ecosystem;
import org.example.entites.ecosystem.diseases.BacterialDisease;
import org.example.entites.ecosystem.diseases.MildiouDisease;
import org.example.entites.ecosystem.diseases.RustDisease;
import org.example.entites.ecosystem.EcosystemCell;
import org.example.entites.ecosystem.diseases.PlantDisease;
import org.example.entites.plant.Plant;
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

        ensureForestCellsInitialized(forest);
        ecosystem = new Ecosystem(forest);
        ecosystem.initialiseEcosystem();

        // Injecte l'écosystème dans le service
        ecosystemService.setEcosystem(ecosystem);
    }

    private void ensureForestCellsInitialized(Forest forest) {
        int x = 0;
        int y = 0;

        for (Plant plant : forest.getPlants()) {
            boolean alreadyAssigned = forest.getCells().stream()
                    .anyMatch(c -> plant.getId().equals(c.getPlantId()));
            if (alreadyAssigned) continue;

            Forest.ForestCell cell = new Forest.ForestCell(x, y, plant.getId());
            forest.addCell(cell);

            x++;
            if (x >= forest.getWidth()) {
                x = 0;
                y++;
            }
        }
    }

    //Créer un écosystème avec une forêt donnée
    private void ensureEcosystemInitialized(String forestId) {
    if (ecosystem != null && ecosystem.getForest().getId().equals(forestId)) return;

    Forest forest = forestRepository.findById(forestId)
            .orElseThrow(() -> new IllegalStateException("Aucune forêt trouvée avec l'ID : " + forestId));

    initEcosystemWithForest(forest);
    }
    
    private void initEcosystemWithForest(Forest forest) {
    // Associer les plantes à des cellules si nécessaire
    for (Plant plant : forest.getPlants()) {
        if (plant.getX() == null || plant.getY() == null) {
            int x = (int) (Math.random() * forest.getWidth());
            int y = (int) (Math.random() * forest.getHeight());
            while (forest.isPositionOccupied(x, y)) {
                x = (int) (Math.random() * forest.getWidth());
                y = (int) (Math.random() * forest.getHeight());
            }
            plant.setX(x);
            plant.setY(y); 
            Forest.ForestCell cell = new Forest.ForestCell(x, y, plant.getId());
            forest.getCells().add(cell);
        }
    }

    ecosystem = new Ecosystem(forest);
    ecosystem.initialiseEcosystem();
    ecosystemService.setEcosystem(ecosystem);

    int nbInitialDiseased = Math.min(15, forest.getPlants().size());
    int infected = 0;
    while(infected < nbInitialDiseased){
        EcosystemCell cell = ecosystem.getCells().get((int) (Math.random() * ecosystem.getCells().size()));
        if(!cell.hasPlant() || cell.isDiseased()) continue;
        
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