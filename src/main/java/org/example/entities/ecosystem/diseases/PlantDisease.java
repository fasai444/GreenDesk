package org.example.entities.ecosystem.diseases;

import org.example.entities.plant.Plant;

public interface PlantDisease {
    void progress(Plant plant);
    double getBaseContagion();
    double getSeverity();
    String getName();
    PlantDisease copy();
    double getInfectionThreshold();
    double getRecoveryThreshold();
}
