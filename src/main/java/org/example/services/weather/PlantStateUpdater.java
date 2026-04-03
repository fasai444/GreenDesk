package org.example.services.weather;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlantStateUpdater {
    
    @Autowired
    private PlantRepository plantRepository;
    
    /**
     * Met à jour l'état de la plante en fonction de l'ISR et du SPS
     */
    public void updatePlantState(Plant plant, double isr, double sps) {
        double newStress = Math.min(1.0, plant.getStressIndex() + (isr * 0.2));
        plant.setStressIndex(newStress);
        
        // Mise à jour de l'état selon ISR ou stressIndex
        if (isr > 0.7 || newStress > 0.8) {
            plant.setPlantState(PlantState.DISEASED);
        } else if (isr > 0.4 || newStress > 0.5) {
            plant.setPlantState(PlantState.STRESSED);
        }
        
        plantRepository.save(plant);
    }
    
    /**
     * Ajustement dynamique des seuils (à compléter dans la Partie 2)
     */
    public void applyDynamicAdjustments(Plant plant, WeatherAlert alert) {
        // À implémenter dans la Partie 2
    }
}