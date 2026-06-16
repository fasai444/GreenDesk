package org.example.entities.ecosystem.diseases;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;

public class RustDisease implements PlantDisease {
    private double severity;

    public RustDisease(double initialSeverity) {
        this.severity = initialSeverity;
    }

    @Override
    public void progress(Plant plant) {
        // Stress progressif
        plant.setStressIndex(Math.min(1.0, plant.getStressIndex() + 0.03 * severity));

        // Croissance réduite plus lentement
        plant.setHeightCm(plant.getHeightCm() * (1 - 0.02 * severity));

        // Augmentation modérée de la sévérité
        severity = Math.min(1.0, severity + 0.015 + Math.random() * 0.01);

        // Mise à jour de l’état
        if (severity > 0.7) {
            plant.setPlantState(PlantState.DISEASED);
        } else if (severity > 0.4) {
            plant.setPlantState(PlantState.STRESSED);
        }
    }

    @Override
    public PlantDisease copy() {
        return new RustDisease(this.getSeverity());
    }

    @Override
    public double getInfectionThreshold() {
        return 0.3;
    }

    @Override
    public double getRecoveryThreshold() {
        return 0.7;
    }

    @Override
    public double getBaseContagion() {
        return 0.10 * severity; // contagion relativement faible
    }

    @Override
    public double getSeverity() {
        return severity;
    }

    @Override
    public String getName() {
        return "Rust";
    }
}
