package org.example.services.weather;

import org.example.entities.plant.GrowthStage;
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
     * Met à jour l'état de la plante en fonction de l'ISR et du SPS.
     * Applique automatiquement les ajustements dynamiques si la plante
     * est en phase critique (floraison / fructification).
     */
    public void updatePlantState(Plant plant, double isr, double sps) {
        double newStress = Math.min(1.0, plant.getStressIndex() + (isr * 0.2));
        plant.setStressIndex(newStress);

        // Seuils de base
        double thresholdDiseasedIsr   = 0.7;
        double thresholdDiseasedStress = 0.8;
        double thresholdStressedIsr   = 0.4;
        double thresholdStressedStress = 0.5;

        // Ajustement dynamique si phase critique
        GrowthStage stage = plant.getGrowthStage();
        if (stage == GrowthStage.FLOWERING || stage == GrowthStage.FRUITING) {
            thresholdDiseasedIsr    = 0.5;
            thresholdDiseasedStress = 0.6;
            thresholdStressedIsr    = 0.3;
            thresholdStressedStress = 0.35;
        }

        if (isr > thresholdDiseasedIsr || newStress > thresholdDiseasedStress) {
            plant.setPlantState(PlantState.DISEASED);
        } else if (isr > thresholdStressedIsr || newStress > thresholdStressedStress) {
            plant.setPlantState(PlantState.STRESSED);
        }

        plantRepository.save(plant);
    }

    /**
     * Ajustement dynamique des seuils selon la phase de croissance et le type d'alerte.
     * Appelée avant updatePlantState pour des ajustements supplémentaires spécifiques
     * au type d'alerte météo (gel critique en floraison, canicule en fructification, etc.)
     */
    public void applyDynamicAdjustments(Plant plant, WeatherAlert alert) {
        GrowthStage stage = plant.getGrowthStage();
        if (stage != GrowthStage.FLOWERING && stage != GrowthStage.FRUITING) {
            return; // Pas de phase critique, rien à ajuster
        }

        String alertType = alert.getType();

        // En floraison : le gel et la canicule sont particulièrement destructeurs
        if (stage == GrowthStage.FLOWERING) {
            if ("frost".equals(alertType) || "heatwave".equals(alertType)) {
                // Augmenter le stressIndex directement (choc thermique critique)
                double boostedStress = Math.min(1.0, plant.getStressIndex() + 0.15);
                plant.setStressIndex(boostedStress);
            }
        }

        // En fructification : la pluie intense et le vent fort abîment les fruits
        if (stage == GrowthStage.FRUITING) {
            if ("heavy_rain".equals(alertType) || "high_wind".equals(alertType)) {
                double boostedStress = Math.min(1.0, plant.getStressIndex() + 0.10);
                plant.setStressIndex(boostedStress);
            }
        }

        plantRepository.save(plant);
    }
}