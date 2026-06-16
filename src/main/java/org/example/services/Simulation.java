package org.example.services;

import org.example.entities.environment.EnvironmentData;
//import org.example.entities.plant.Plant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Simulation {

    private final EnvironmentService envService;
    private final EnvironmentData environment; // ton unique environnement

    public Simulation(EnvironmentService envService, EnvironmentData environment) {
        this.envService = envService;
        this.environment = environment;
    }

    public EnvironmentData getEnvironment() {
        return environment;
    }

    /**
     * Simulation manuelle pour X heures (bouton utilisateur)
     */
    public void simulate(int nbHeures) {
        envService.simulate(environment, nbHeures);
    }

    /**
     * Tick automatique toutes les heures (production)
     */
    @Scheduled(fixedRate = 3600000) // toutes les heures
    public void runHourlySimulation() {
        envService.tick(environment);
    }
}
