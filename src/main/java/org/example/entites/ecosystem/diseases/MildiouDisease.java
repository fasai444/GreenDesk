package org.example.entites.ecosystem.diseases;

import org.example.entites.plant.Plant;
import org.example.entites.plant.PlantState;

public class MildiouDisease implements PlantDisease {
    private double severity;

    public MildiouDisease(double initialSeverity) {
        this.severity = initialSeverity;
    }

    @Override
    public void progress(Plant plant) {
        // Impact modéré du stress
        double stressIncrease = 0.04 * severity;
        plant.setStressIndex(Math.min(1.0, plant.getStressIndex() + stressIncrease));

        // Réduction de croissance proportionnelle à la sévérité
        plant.setHeightCm(plant.getHeightCm() * (1 - 0.025 * severity));

        // Augmenter progressivement la sévérité
        severity = Math.min(1.0, severity + 0.02 + Math.random() * 0.01);

        // Mise à jour de l’état
        if (severity > 0.6) {
            plant.setPlantState(PlantState.DISEASED);
        } else if (severity > 0.3) {
            plant.setPlantState(PlantState.STRESSED);
        }
    }

    @Override
    public PlantDisease copy() {
        return new MildiouDisease(this.getSeverity()); // ou 0.1 si tu veux repartir de faible sévérité
    }

    @Override
    public double getInfectionThreshold() {
        return 0.5; // contagion modérée
    }

    @Override
    public double getRecoveryThreshold() {
        return 0.6; // guérison assez lente
    }

    @Override
    public double getBaseContagion() {
        return 0.15 * severity; // contagion modérée
    }

    @Override
    public double getSeverity() {
        return severity;
    }

    @Override
    public String getName() {
        return "Mildiou";
    }
}