package org.example.services.weather;

import org.example.entities.plant.Plant;
import org.example.entities.plant.GrowthStage;
import org.example.entities.weather.WeatherAlert;
import org.example.entities.weather.PlantImpact;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantImpactCalculator {
    
    // Seuils par type d'alerte [critique, danger]
    private static final java.util.Map<String, double[]> THRESHOLDS = java.util.Map.of(
        "heatwave", new double[]{30.0, 40.0},
        "frost", new double[]{2.0, -5.0},
        "heavy_rain", new double[]{10.0, 30.0},
        "high_wind", new double[]{50.0, 100.0},
        "uv_alert", new double[]{6.0, 11.0}
    );
    
    /**
     * Calcule l'Indice de Stress des Plantes (ISR)
     * @param plant Plante concernée
     * @param alert Alerte météo reçue
     * @return ISR entre 0 et 1
     */
    public double calculateISR(Plant plant, WeatherAlert alert) {
        String type = alert.getType();
        double valeur = extractValue(alert, type);
        double[] seuils = THRESHOLDS.getOrDefault(type, new double[]{0, 0});
        
        double tempExcès = computeExcess(valeur, seuils[0], seuils[1]);
        
        // Formule ISR simplifiée pour la Partie 1
        double isr = tempExcès * 0.4;
        
        // Ajouter d'autres facteurs selon le type d'alerte
        if ("heavy_rain".equals(type)) {
            isr += computeExcess(valeur, seuils[0], seuils[1]) * 0.3;
        } else if ("high_wind".equals(type)) {
            isr += computeExcess(valeur, seuils[0], seuils[1]) * 0.2;
        } else if ("uv_alert".equals(type)) {
            isr += computeExcess(valeur, seuils[0], seuils[1]) * 0.1;
        }
        
        return Math.min(1.0, Math.max(0.0, isr));
    }
    
    /**
     * Calcule le Score de Priorité des Soins (SPS)
     * @param plant Plante concernée
     * @param isr ISR calculé
     * @param history Historique des impacts
     * @return SPS entre 0 et 1
     */
    public double calculateSPS(Plant plant, double isr, List<PlantImpact> history) {
        double phaseCritique = isPhaseCritique(plant) ? 1.0 : 0.0;
        double historique = computeHistoricalStress(history);
        
        return (isr * 0.5) + (phaseCritique * 0.3) + (historique * 0.2);
    }
    
    /**
     * Vérifie si la plante est en phase critique (floraison/fructification)
     */
    private boolean isPhaseCritique(Plant plant) {
        GrowthStage stage = plant.getGrowthStage();
        return stage == GrowthStage.FLOWERING || stage == GrowthStage.FRUITING;
    }
    
    /**
     * Calcule le stress historique moyen sur les 7 derniers jours
     */
    private double computeHistoricalStress(List<PlantImpact> history) {
        if (history == null || history.isEmpty()) {
            return 0.0;
        }
        int limit = Math.min(7, history.size());
        return history.stream()
                .limit(limit)
                .mapToDouble(PlantImpact::getIsr)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Extrait la valeur pertinente de l'alerte selon son type
     */
    private double extractValue(WeatherAlert alert, String type) {
        java.util.Map<String, Object> details = alert.getDetails();
        if (details == null) return 0.0;
        
        switch (type) {
            case "heatwave":
            case "frost":
                return details.containsKey("temperature") ? 
                       ((Number) details.get("temperature")).doubleValue() : 0.0;
            case "heavy_rain":
                return details.containsKey("precipitation") ? 
                       ((Number) details.get("precipitation")).doubleValue() : 0.0;
            case "high_wind":
                return details.containsKey("wind_speed") ? 
                       ((Number) details.get("wind_speed")).doubleValue() : 0.0;
            case "uv_alert":
                return details.containsKey("uv_index") ? 
                       ((Number) details.get("uv_index")).doubleValue() : 0.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * Calcule l'excès normalisé par rapport aux seuils
     */
    private double computeExcess(double value, double critical, double danger) {
        if (value <= critical) return 0.0;
        if (value >= danger) return 1.0;
        return (value - critical) / (danger - critical);
    }
}