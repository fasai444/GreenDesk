package org.example.services.scheduling;

import org.example.entities.plant.GrowthStage;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.entities.weather.PlantImpact;
import org.example.services.weather.WeatherForecastService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Moteur WNS (Hybrid Engine) : transforme les besoins biologiques en score de soin.
 *
 * WNS = (0.3 × Taille) + (0.2 × Stade) + (0.15 × Stress) - (0.25 × Pluie_Prévue)
 */
@Service
public class WnsCalculator {

    private final WeatherForecastService weatherForecastService;

    public WnsCalculator(WeatherForecastService weatherForecastService) {
        this.weatherForecastService = weatherForecastService;
    }

    public WnsResult calculate(Plant plant, List<PlantImpact> impactHistory) {
        double taille = computeTailleFactor(plant);
        double stade = computeStadeFactor(plant.getGrowthStage());
        double stress = computeStressFactor(plant, impactHistory);

        var rainForecast = weatherForecastService.getRainForecast(plant.getForestId());
        double pluiePrevue = rainForecast.normalizedIntensity();
        boolean rainWithin6h = rainForecast.rainWithin6Hours();

        double wns = (0.3 * taille) + (0.2 * stade) + (0.15 * stress) - (0.25 * pluiePrevue);

        if (rainWithin6h) {
            wns -= 0.25 * pluiePrevue;
        }

        boolean skipWatering = rainWithin6h && pluiePrevue >= 0.3;

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("taille", round3(taille));
        breakdown.put("stade", round3(stade));
        breakdown.put("stress", round3(stress));
        breakdown.put("pluiePrevue", round3(pluiePrevue));
        breakdown.put("rainWithin6Hours", rainWithin6h);
        breakdown.put("globalScore", round3(wns));

        Optional<PlantImpact> latest = impactHistory == null || impactHistory.isEmpty()
                ? Optional.empty()
                : Optional.of(impactHistory.get(0));
        latest.ifPresent(impact -> {
            breakdown.put("isr", round3(impact.getIsr()));
            breakdown.put("sps", round3(impact.getSps()));
        });

        return new WnsResult(wns, breakdown, rainWithin6h, skipWatering);
    }

    /**
     * Taille normalisée : hauteur en mètres (0 à ~3+).
     */
    double computeTailleFactor(Plant plant) {
        double heightCm = plant.getHeightCm();
        if (heightCm <= 0) {
            return 0.3;
        }
        Species species = plant.getSpecies();
        double maxHeight = species != null ? species.getMaxHeight() : 100.0;
        double normalized = heightCm / Math.max(maxHeight, 1.0);
        return Math.min(3.0, (heightCm / 100.0) * (0.5 + normalized));
    }

    double computeStadeFactor(GrowthStage stage) {
        if (stage == null) {
            return 0.4;
        }
        return switch (stage) {
            case SEEDLING -> 0.2;
            case VEGETATIVE -> 0.5;
            case FLOWERING -> 0.8;
            case FRUITING -> 1.0;
            case MATURE -> 0.7;
        };
    }

    /**
     * Stress combiné : stress biologique de la plante + ISR météo (Feature 1).
     */
    double computeStressFactor(Plant plant, List<PlantImpact> impactHistory) {
        double plantStress = plant.getStressIndex();
        if (plantStress > 1.0) {
            plantStress = plantStress / 100.0;
        }

        double isr = 0.0;
        if (impactHistory != null && !impactHistory.isEmpty()) {
            isr = impactHistory.get(0).getIsr();
        }

        return Math.min(1.0, Math.max(plantStress, isr));
    }

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
