package org.example.services.weather;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantMeasurement;
import org.example.entities.plant.PlantState;
import org.example.repositories.PlantMeasurementRepository;
import org.example.repositories.PlantRepository;
import org.example.services.PlantAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantCalibrationService {

    private static final double LEARNING_RATE = 0.2;      // Vitesse d'apprentissage (20%)
    private static final double MAX_SENSITIVITY = 1.5;    // Sensibilité maximale
    private static final double MIN_SENSITIVITY = 0.5;    // Sensibilité minimale

    @Autowired
    private PlantMeasurementRepository measurementRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private PlantAlertService plantAlertService;

    /**
     * Enregistre une mesure manuelle et recalibre la plante
     */
    public PlantMeasurement recordMeasurement(String plantId, Double measuredHeightCm, Double waterAddedMl, String observedState, String notes) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plante non trouvée: " + plantId));

        PlantMeasurement measurement = new PlantMeasurement(plantId, measuredHeightCm, waterAddedMl, observedState, notes);
        measurement = measurementRepository.save(measurement);

        // Recalibrer la plante après la mesure
        calibratePlant(plant, measurement);

        // Recharger la plante depuis la BDD pour avoir les dernières valeurs (après PUT éventuel)
        plant = plantRepository.findById(plantId).orElse(plant);

        // Déclencher l’évaluation des alertes après la mesure
        plantAlertService.evaluateAndCreateAlerts(plant);

        return measurement;
    }

    /**
     * Recalibre les paramètres de la plante
     */
    private void calibratePlant(Plant plant, PlantMeasurement measurement) {
        // 1. Calibration du facteur de croissance (basée sur la hauteur)
        if (measurement.getMeasuredHeightCm() != null) {
            calibrateGrowthFactor(plant, measurement.getMeasuredHeightCm());
        }

        // 2. Calibration des sensibilités (basée sur l'état observé)
        if (measurement.getObservedState() != null && !measurement.getObservedState().isEmpty()) {
            calibrateSensitivities(plant, measurement.getObservedState());
        }

        plantRepository.save(plant);
    }

    /**
     * Calibre le facteur de croissance en comparant hauteur mesurée vs simulée
     */
    private void calibrateGrowthFactor(Plant plant, double measuredHeight) {
        double expectedHeight = plant.getHeightCm();
        double error = measuredHeight - expectedHeight;

        // Ajustement du facteur de croissance
        double newGrowthFactor = plant.getGrowthFactor() + LEARNING_RATE * (error / Math.max(expectedHeight, 1.0));
        plant.setGrowthFactor(clamp(newGrowthFactor, MIN_SENSITIVITY, MAX_SENSITIVITY));
    }

    /**
     * Calibre les sensibilités en comparant l'état observé vs l'état simulé
     */
    private void calibrateSensitivities(Plant plant, String observedStateStr) {
        PlantState observedState;
        try {
            observedState = PlantState.valueOf(observedStateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return; // État invalide, on ignore
        }

        PlantState simulatedState = plant.getPlantState();
        double currentStress = plant.getStressIndex();

        // Si l'état observé est meilleur que l'état simulé → la plante est plus résistante
        // → on diminue les sensibilités
        // Si l'état observé est pire que l'état simulé → la plante est plus sensible
        // → on augmente les sensibilités

        int delta = getStateValue(observedState) - getStateValue(simulatedState);

        if (delta > 0) {
            // La plante va mieux que prévu → moins sensible
            plant.setWaterSensitivity(clamp(plant.getWaterSensitivity() - LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setTempSensitivity(clamp(plant.getTempSensitivity() - LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setLightSensitivity(clamp(plant.getLightSensitivity() - LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
        } else if (delta < 0) {
            // La plante va moins bien que prévu → plus sensible
            plant.setWaterSensitivity(clamp(plant.getWaterSensitivity() + LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setTempSensitivity(clamp(plant.getTempSensitivity() + LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setLightSensitivity(clamp(plant.getLightSensitivity() + LEARNING_RATE * 0.1, MIN_SENSITIVITY, MAX_SENSITIVITY));
        }

        // Ajustement supplémentaire basé sur le stress
        // Si le stress est élevé mais que la plante va bien → on a sur-estimé le stress → baisser sensibilités
        // Si le stress est faible mais que la plante va mal → on a sous-estimé le stress → augmenter sensibilités

        boolean stressHigh = currentStress > 0.6;
        boolean stateGood = observedState == PlantState.HEALTHY || observedState == PlantState.STRESSED;

        if (stressHigh && stateGood) {
            // Surestimation du stress → baisser sensibilités
            plant.setWaterSensitivity(clamp(plant.getWaterSensitivity() - LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setTempSensitivity(clamp(plant.getTempSensitivity() - LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setLightSensitivity(clamp(plant.getLightSensitivity() - LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
        } else if (!stressHigh && !stateGood) {
            // Sous-estimation du stress → augmenter sensibilités
            plant.setWaterSensitivity(clamp(plant.getWaterSensitivity() + LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setTempSensitivity(clamp(plant.getTempSensitivity() + LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
            plant.setLightSensitivity(clamp(plant.getLightSensitivity() + LEARNING_RATE * 0.05, MIN_SENSITIVITY, MAX_SENSITIVITY));
        }
    }

    /**
     * Convertit un état en valeur numérique pour comparaison
     * HEALTHY = 3, STRESSED = 2, DORMANT = 1, DISEASED = 0
     */
    private int getStateValue(PlantState state) {
        if (state == null) return 1;
        switch (state) {
            case HEALTHY: return 3;
            case STRESSED: return 2;
            case DORMANT: return 1;
            case DISEASED: return 0;
            default: return 1;
        }
    }

    /**
     * Limite une valeur entre min et max
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Récupère l'historique des mesures pour une plante
     */
    public List<PlantMeasurement> getMeasurementHistory(String plantId) {
        return measurementRepository.findByPlantIdOrderByTimestampDesc(plantId);
    }
}
