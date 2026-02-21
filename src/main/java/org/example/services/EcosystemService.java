package org.example.services;

import org.example.entites.ecosystem.Ecosystem;
import org.example.entites.ecosystem.EcosystemCell;
import org.example.entites.ecosystem.diseases.PlantDisease;
import org.example.entites.plant.Plant;
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
}