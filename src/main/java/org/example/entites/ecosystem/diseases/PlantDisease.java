package org.example.entites.ecosystem.diseases;

import org.example.entites.plant.Plant;

public interface PlantDisease {
    void progress(Plant plant);
    double getBaseContagion();
    double getSeverity();
    String getName();
    PlantDisease copy();
    double getInfectionThreshold();
    double getRecoveryThreshold();
}
