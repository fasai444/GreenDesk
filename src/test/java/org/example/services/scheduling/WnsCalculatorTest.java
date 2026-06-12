package org.example.services.scheduling;

import org.example.entities.plant.GrowthStage;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.entities.weather.PlantImpact;
import org.example.services.weather.WeatherForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WnsCalculatorTest {

    @Mock
    private WeatherForecastService weatherForecastService;

    private WnsCalculator wnsCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        wnsCalculator = new WnsCalculator(weatherForecastService);
    }

    @Test
    @DisplayName("WNS = 0.3×Taille + 0.2×Stade + 0.15×Stress - 0.25×Pluie")
    void shouldComputeWnsFormula() {
        Plant plant = new Plant();
        plant.setId("p1");
        plant.setForestId("f1");
        plant.setHeightCm(200);
        plant.setGrowthStage(GrowthStage.FLOWERING);
        plant.setStressIndex(0.8);
        Species species = new Species("Oak", 5000, 20, 60, 5000, 1, 0.5);
        species.setMaxHeight(300);
        plant.setSpecies(species);

        when(weatherForecastService.getRainForecast("f1"))
                .thenReturn(new WeatherForecastService.RainForecast(0.0, false));

        WnsResult result = wnsCalculator.calculate(plant, Collections.emptyList());

        double taille = result.getBreakdown().get("taille") instanceof Number n ? n.doubleValue() : 0;
        double stade = result.getBreakdown().get("stade") instanceof Number n ? n.doubleValue() : 0;
        double stress = result.getBreakdown().get("stress") instanceof Number n ? n.doubleValue() : 0;
        double pluie = result.getBreakdown().get("pluiePrevue") instanceof Number n ? n.doubleValue() : 0;

        double expected = (0.3 * taille) + (0.2 * stade) + (0.15 * stress) - (0.25 * pluie);
        assertEquals(expected, result.getScore(), 0.001);
        assertTrue(result.getScore() > WnsResult.THRESHOLD);
    }

    @Test
    @DisplayName("Pluie dans 6h réduit le WNS et bloque l'arrosage inutile")
    void shouldAdjustWnsWhenRainWithin6Hours() {
        Plant plant = new Plant();
        plant.setId("p2");
        plant.setForestId("f1");
        plant.setHeightCm(150);
        plant.setGrowthStage(GrowthStage.VEGETATIVE);
        plant.setStressIndex(0.9);

        when(weatherForecastService.getRainForecast("f1"))
                .thenReturn(new WeatherForecastService.RainForecast(25.0, true));

        WnsResult result = wnsCalculator.calculate(plant, Collections.emptyList());

        assertTrue(result.isRainWithin6Hours());
        assertTrue(result.isSkipWatering());
        assertTrue((Double) result.getBreakdown().get("pluiePrevue") > 0);
    }

    @Test
    @DisplayName("ISR météo (Feature 1) intègre le stress dans le calcul")
    void shouldUseIsrFromFeature1() {
        Plant plant = new Plant();
        plant.setId("p3");
        plant.setForestId("f1");
        plant.setStressIndex(0.2);

        PlantImpact impact = new PlantImpact("alert-1", "p3", 0.9, 0.85,
                0.2, 0.4, "HEALTHY", "STRESSED", List.of());

        when(weatherForecastService.getRainForecast("f1"))
                .thenReturn(new WeatherForecastService.RainForecast(0.0, false));

        WnsResult result = wnsCalculator.calculate(plant, List.of(impact));

        assertEquals(0.9, result.getBreakdown().get("isr"));
        assertEquals(0.85, result.getBreakdown().get("sps"));
        assertEquals(0.9, result.getBreakdown().get("stress"));
    }
}
