package org.example.services;

import org.example.entites.environment.EnvironmentData;
import org.example.entites.plant.Plant;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentServices {

    private final PlantServices plantServices;

    public EnvironmentServices(PlantServices plantServices) {
        this.plantServices = plantServices;
    }



    /**
     * Fait évoluer l'environnement d'une heure et toutes ses plantes
     */
    public void tick(EnvironmentData env) {
        env.evolve();

        for (Plant plant : env.getPlants()) {
            plantServices.evolvePlant(plant, env);
        }
    }

    /**
     * Fait évoluer l'environnement de plusieurs heures
     */
    public void simulate(EnvironmentData env, int nbHeures) {
        for (int i = 0; i < nbHeures; i++) {
            tick(env);
        }
    }
}
