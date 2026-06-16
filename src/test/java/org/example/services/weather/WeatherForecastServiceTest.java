package org.example.services.weather;

import org.example.entities.forest.Forest;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.ForestRepository;
import org.example.repositories.WeatherAlertRepository;
import org.example.util.ParisTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class WeatherForecastServiceTest {

    @Mock
    private ForestRepository forestRepository;

    @Mock
    private WeatherAlertRepository weatherAlertRepository;

    private WeatherForecastService weatherForecastService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherForecastService = new WeatherForecastService(forestRepository, weatherAlertRepository);
    }

    @Test
    void shouldIgnoreRainAlertsOutsideTargetForestRadius() {
        Forest forest = new Forest();
        forest.setCoords(new double[]{48.8566, 2.3522});

        WeatherAlert distantRain = rainAlert(new double[]{45.7640, 4.8357});
        when(forestRepository.findById("forest-paris")).thenReturn(Optional.of(forest));
        when(weatherAlertRepository.findAll()).thenReturn(List.of(distantRain));

        WeatherForecastService.RainForecast forecast = ReflectionTestUtils.invokeMethod(
                weatherForecastService, "fromAlertsForForest", "forest-paris"
        );

        assertFalse(forecast.rainWithin6Hours());
    }

    @Test
    void shouldUseRainAlertsNearTargetForest() {
        Forest forest = new Forest();
        forest.setCoords(new double[]{48.8566, 2.3522});

        WeatherAlert nearbyRain = rainAlert(new double[]{48.8600, 2.3600});
        when(forestRepository.findById("forest-paris")).thenReturn(Optional.of(forest));
        when(weatherAlertRepository.findAll()).thenReturn(List.of(nearbyRain));

        WeatherForecastService.RainForecast forecast = ReflectionTestUtils.invokeMethod(
                weatherForecastService, "fromAlertsForForest", "forest-paris"
        );

        assertTrue(forecast.rainWithin6Hours());
    }

    private WeatherAlert rainAlert(double[] coords) {
        return new WeatherAlert(
                "rain-event",
                "heavy_rain",
                coords,
                ParisTime.now().plusHours(1),
                "high",
                Map.of("precipitation", 20.0)
        );
    }
}
