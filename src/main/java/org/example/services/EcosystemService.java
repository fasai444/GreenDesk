package org.example.services;

import org.example.entities.ecosystem.Ecosystem;
import org.example.entities.ecosystem.EcosystemCell;
import org.example.entities.ecosystem.diseases.PlantDisease;
import org.example.entities.ecosystem.diseases.BacterialDisease;
import org.example.entities.ecosystem.diseases.MildiouDisease;
import org.example.entities.ecosystem.diseases.RustDisease;
import org.example.entities.plant.Plant;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de simulation de l'écosystème végétal d'une forêt.
 * Gère la propagation des maladies et la progression de celles-ci.
 */
@Service
public class EcosystemService {

    private Ecosystem ecosystem; // injecté via setter ou initialisation dans les tests

    private final PlantRepository plantRepository;

    // Constructeur Spring uniquement avec les beans connus
    @Autowired
    public EcosystemService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    public void setEcosystem(Ecosystem ecosystem) {
        this.ecosystem = ecosystem;
    }

    public void tick() {
        if (ecosystem == null) {
            throw new IllegalStateException("Ecosystem n'est pas défini !");
        }

        List<PlannedChange> plannedChanges = new ArrayList<>();

        for (EcosystemCell cell : ecosystem.getCells()) {
            if (!cell.hasPlant()) continue;

            Plant plant = getPlantById(cell.getForestCell().getPlantId());
            if (plant == null) continue;

            if (cell.isDiseased()) cell.progressDisease(plant);

            if (cell.isDiseased()) {
                double recoveryThreshold = cell.getDisease().getRecoveryThreshold();
                if (cell.shouldRecover(ecosystem, recoveryThreshold)) {
                    plannedChanges.add(new PlannedChange(cell, true, null));
                    continue;
                }
            }

            if (!cell.isDiseased()) {
                PlantDisease dominantDisease = cell.getMostSevereNeighborDisease(ecosystem);
                if (dominantDisease != null) {
                    double infectionThreshold = dominantDisease.getInfectionThreshold();
                    if (cell.shouldBecomeInfected(ecosystem, infectionThreshold)) {
                        plannedChanges.add(new PlannedChange(cell, false, dominantDisease.copy()));
                    }
                }
            }
        }

        for (PlannedChange change : plannedChanges) {
            if (change.shouldRecover) {
                change.cell.recover();
            } else if (change.newDisease != null) {
                change.cell.infect(change.newDisease);
            }
        }
    }

    public void simulateTicks(int n) {
        for (int i = 1; i <= n; i++) {
            tick();
            System.out.println("=== Tick " + i + " ===");
            printEcosystemState();
        }
    }

    public void printEcosystemState() {
        for (EcosystemCell cell : ecosystem.getCells()) {
            String plantId = cell.hasPlant() ? cell.getForestCell().getPlantId() : "empty";
            String disease = cell.isDiseased() ? cell.getDisease().getName() + "("
                    + String.format("%.2f", cell.getDisease().getSeverity()) + ")" : "healthy";
            System.out.println("Cell [" + cell.getForestCell().getX() + "," + cell.getForestCell().getY() + "] - Plant: " + plantId + " - State: " + disease);
        }
    }

    public List<EcosystemCell> getEcosystemCells() {
        if (ecosystem == null) return List.of();
        return new ArrayList<>(ecosystem.getCells());
    }

    private Plant getPlantById(String plantId) {
        return plantRepository.findById(plantId).orElse(null);
    }

    public List<String> getEcosystemStatus() {
        if (ecosystem == null) return List.of();
        List<String> status = new ArrayList<>();
        for (EcosystemCell cell : ecosystem.getCells()) {
            String plantId = cell.hasPlant() ? cell.getForestCell().getPlantId() : "empty";
            String disease = cell.isDiseased() ? cell.getDisease().getName() + "("
                    + String.format("%.2f", cell.getDisease().getSeverity()) + ")" : "healthy";
            status.add("Cell [" + cell.getForestCell().getX() + "," + cell.getForestCell().getY() + "] - Plant: "
                    + plantId + " - State: " + disease);
        }
        return status;
    }

    // Classe interne pour gérer les changements planifiés
    private static class PlannedChange {
        EcosystemCell cell;
        boolean shouldRecover;
        PlantDisease newDisease;

        public PlannedChange(EcosystemCell cell, boolean shouldRecover, PlantDisease newDisease) {
            this.cell = cell;
            this.shouldRecover = shouldRecover;
            this.newDisease = newDisease;
        }
    }

    /**
 * Infecte des cellules spécifiques avec une maladie donnée ou aléatoire.
 * @param cellCoordinates Liste de coordonnées [x, y] des cellules à infecter.
 * @param diseaseInstance Instance de PlantDisease à utiliser, ou null pour en choisir une aléatoire.
 * @param initialSeverity Sévérité initiale (0.0 à 1.0) si aléatoire.
 */
    public void seedDiseasesFlexible(List<int[]> cellCoordinates, PlantDisease diseaseInstance, double initialSeverity) {
    if (ecosystem == null) return;

    for (int[] coords : cellCoordinates) {
        int x = coords[0];
        int y = coords[1];

        EcosystemCell cell = ecosystem.getCells().stream()
            .filter(c -> c.getForestCell().getX() == x && c.getForestCell().getY() == y)
            .findFirst()
            .orElse(null);
        if (cell == null || !cell.hasPlant() || cell.isDiseased()) continue;

        PlantDisease disease;

        if (diseaseInstance == null) {
            // Choix aléatoire parmi les maladies disponibles
            int random = (int) (Math.random() * 3);
            disease = switch (random) {
                case 0 -> new BacterialDisease(initialSeverity);
                case 1 -> new MildiouDisease(initialSeverity);
                default -> new RustDisease(initialSeverity);
            };
        } else {
            // Utiliser la maladie donnée
            disease = diseaseInstance.copy(); // pour éviter de partager la même instance
        }

        cell.infect(disease);
        System.out.println("Cell [" + x + "," + y + "] infectée avec " + disease.getName());
        }
    }

    // Infecte une cellule donnée avec une maladie
    public void infectCell(int x, int y, PlantDisease disease) {
        if (ecosystem == null || disease == null) return;

        EcosystemCell cell = ecosystem.getCells().stream()
                .filter(c -> c.getForestCell().getX() == x && c.getForestCell().getY() == y)
                .findFirst()
                .orElse(null);

        if (cell != null && cell.hasPlant()) {
            cell.infect(disease);
        }
}
}