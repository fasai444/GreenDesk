package org.example.entities.ecosystem.diseases;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;

public class BacterialDisease implements PlantDisease {
    private double severity;

    public BacterialDisease(double initialSeverity) {
        this.severity = initialSeverity;
    }

    @Override
    public void progress(Plant plant) {
        // Perte d’eau interne → stress sévère
        double waterLossFactor = 0.1 * severity;
        plant.setWaterLevel(plant.getWaterLevel() * (1 - waterLossFactor));

        // Stress fort
        plant.setStressIndex(Math.min(1.0, plant.getStressIndex() + 0.1 * severity));

        // Augmentation rapide de la sévérité
        severity = Math.min(1.0, severity + 0.03 + Math.random() * 0.02);

        // Mise à jour de l’état
        if (severity > 0.5) {
            plant.setPlantState(PlantState.DISEASED);
        }
    }

    @Override
    public PlantDisease copy() {
        return new BacterialDisease(this.getSeverity());
    }

    @Override
    public double getInfectionThreshold() {
        return 0.7; // plus contagieux
    }

    @Override
    public double getRecoveryThreshold() {
        return 0.4; // guérison plus difficile
    }

    @Override
    public double getBaseContagion() {
        return 0.25 * severity; // contagion plus élevée
    }

    @Override
    public double getSeverity() {
        return severity;
    }

    @Override
    public String getName() {
        return "BacterialWilt";
    }
}
