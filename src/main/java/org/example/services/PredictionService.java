package org.example.services;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.example.entities.species.Species;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictionService {

    @Autowired
    private PlantRepository plantRepository;

    /**
     * Prédit l'évolution du stress et de la hauteur pour une plante sur les 7 prochains jours
     * @param plantId ID de la plante
     * @param days Nombre de jours à prédire (défaut 7)
     * @return Map contenant les prédictions
     */
    public Map<String, Object> predictPlantEvolution(String plantId, int days) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plante non trouvée: " + plantId));

        List<PredictionPoint> stressPredictions = new ArrayList<>();
        List<PredictionPoint> heightPredictions = new ArrayList<>();

        double currentStress = plant.getStressIndex();
        double currentHeight = plant.getHeightCm();
        Species species = plant.getSpecies();

        // Facteurs de prédiction
        double growthRate = species.getBaseGrowthRate() * plant.getGrowthFactor();
        double stressDecayRate = 0.1; // Le stress diminue naturellement de 10% par jour
        double stressIncreaseFactor = 0.05; // Augmentation quotidienne naturelle du stress

        LocalDate startDate = LocalDate.now();

        for (int i = 1; i <= days; i++) {
            LocalDate date = startDate.plusDays(i);

            // Prédiction du stress (modèle simplifié)
            // Le stress a tendance à augmenter légèrement, mais peut diminuer si la plante est en bonne santé
            double predictedStress;
            if (currentStress < 0.3) {
                // Plante en bonne santé → stress augmente lentement
                predictedStress = Math.min(1.0, currentStress + stressIncreaseFactor * 0.5);
            } else if (currentStress > 0.7) {
                // Plante en danger → stress diminue lentement (si amélioration)
                predictedStress = Math.max(0.0, currentStress - stressDecayRate * 0.3);
            } else {
                // Stress modéré
                predictedStress = currentStress + (Math.random() - 0.5) * 0.1;
            }
            predictedStress = Math.max(0.0, Math.min(1.0, predictedStress));
            stressPredictions.add(new PredictionPoint(date, predictedStress));

            // Prédiction de la hauteur (croissance logistique)
            double maxHeight = species.getMaxHeight();
            double remainingGrowth = maxHeight - currentHeight;
            double dailyGrowth = growthRate * (remainingGrowth / maxHeight) * (1 - currentStress * 0.5);
            double predictedHeight = Math.min(maxHeight, currentHeight + dailyGrowth);
            heightPredictions.add(new PredictionPoint(date, predictedHeight));

            currentStress = predictedStress;
            currentHeight = predictedHeight;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("plantId", plantId);
        result.put("plantName", plant.getName());
        result.put("currentStress", plant.getStressIndex());
        result.put("currentHeight", plant.getHeightCm());
        result.put("stressPredictions", stressPredictions);
        result.put("heightPredictions", heightPredictions);

        // Ajouter une alerte prédictive si le stress risque d'atteindre un seuil critique
        double maxPredictedStress = stressPredictions.stream()
                .mapToDouble(PredictionPoint::getValue)
                .max()
                .orElse(0);
        
        if (maxPredictedStress > 0.7) {
            result.put("alert", Map.of(
                "type", "HIGH_STRESS_RISK",
                "message", "⚠️ Risque élevé de stress dans les " + days + " prochains jours",
                "maxPredictedStress", maxPredictedStress
            ));
        }

        return result;
    }

    /**
     * Point de prédiction (date + valeur)
     */
    public static class PredictionPoint {
        private LocalDate date;
        private double value;

        public PredictionPoint(LocalDate date, double value) {
            this.date = date;
            this.value = value;
        }

        public LocalDate getDate() { return date; }
        public double getValue() { return value; }
    }
}
