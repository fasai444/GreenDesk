package org.example.services.weather;

import org.example.entities.forest.Forest;
import org.example.entities.weather.WeatherAlert;
import org.example.util.ParisTime;
import org.example.repositories.ForestRepository;
import org.example.repositories.WeatherAlertRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fournit les prévisions de pluie pour le moteur WNS.
 *
 * Chaîne de sources (sans clé obligatoire) :
 * 1. Tomorrow.io (si TOMORROW_API_KEY)
 * 2. Open-Meteo (gratuit, sans clé)
 * 3. Alertes météo Feature 1 en base (webhook / simulation)
 */
@Service
public class WeatherForecastService {

  public record RainForecast(double precipitationMm, boolean rainWithin6Hours) {
    public double normalizedIntensity() {
      return Math.min(1.0, precipitationMm / 50.0);
    }
  }

  private static final List<String> RAIN_ALERT_TYPES = List.of("heavy_rain", "rain");

  private final ForestRepository forestRepository;
  private final WeatherAlertRepository weatherAlertRepository;
  private final RestTemplate restTemplate;

  @Value("${tomorrow.api.key:}")
  private String tomorrowApiKey;

  public WeatherForecastService(ForestRepository forestRepository,
                                WeatherAlertRepository weatherAlertRepository) {
    this.forestRepository = forestRepository;
    this.weatherAlertRepository = weatherAlertRepository;
    this.restTemplate = new RestTemplate();
  }

  public RainForecast getRainForecast(String forestId) {
    if (forestId == null) {
      return fromAlertsOnly();
    }

    Optional<Forest> forestOpt = forestRepository.findById(forestId);
    if (forestOpt.isEmpty() || forestOpt.get().getCoords() == null) {
      return fromAlertsOnly();
    }

    double[] coords = forestOpt.get().getCoords();

    RainForecast apiForecast = fetchFromTomorrowIo(coords);
    if (apiForecast != null) {
      return apiForecast;
    }

    RainForecast openMeteoForecast = fetchFromOpenMeteo(coords);
    if (openMeteoForecast != null) {
      return openMeteoForecast;
    }

    return fromAlertsForForest(forestId);
  }

  private RainForecast fetchFromTomorrowIo(double[] coords) {
    if (tomorrowApiKey == null || tomorrowApiKey.isBlank()) {
      return null;
    }

    try {
      double lat = coords[0];
      double lon = coords[1];

      String url = String.format(
          "https://api.tomorrow.io/v4/weather/forecast?location=%f,%f&timesteps=1h&units=metric&fields=precipitationIntensity",
          lat, lon
      );

      HttpHeaders headers = new HttpHeaders();
      headers.set("apikey", tomorrowApiKey);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        return null;
      }

      return parseTomorrowResponse(response.getBody());
    } catch (Exception e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private RainForecast parseTomorrowResponse(Map<String, Object> body) {
    try {
      List<Map<String, Object>> timelines = (List<Map<String, Object>>) body.get("timelines");
      if (timelines == null || timelines.isEmpty()) {
        return null;
      }

      List<Map<String, Object>> intervals = (List<Map<String, Object>>) timelines.get(0).get("intervals");
      if (intervals == null) {
        return null;
      }

      double maxPrecip6h = 0.0;
      boolean rainWithin6h = false;
      int hoursToCheck = Math.min(6, intervals.size());

      for (int i = 0; i < hoursToCheck; i++) {
        Map<String, Object> interval = intervals.get(i);
        Map<String, Object> values = (Map<String, Object>) interval.get("values");
        if (values == null) {
          continue;
        }
        Object precip = values.get("precipitationIntensity");
        if (precip instanceof Number number) {
          double mm = number.doubleValue();
          if (mm > 0.5) {
            rainWithin6h = true;
          }
          maxPrecip6h = Math.max(maxPrecip6h, mm);
        }
      }

      return new RainForecast(maxPrecip6h, rainWithin6h);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Open-Meteo : API météo gratuite, sans clé API.
   * https://open-meteo.com/
   */
  @SuppressWarnings("unchecked")
  private RainForecast fetchFromOpenMeteo(double[] coords) {
    try {
      double lat = coords[0];
      double lon = coords[1];

      String url = String.format(
          "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f"
              + "&hourly=precipitation&forecast_hours=6",
          lat, lon
      );

      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        return null;
      }

      Map<String, Object> hourly = (Map<String, Object>) response.getBody().get("hourly");
      if (hourly == null) {
        return null;
      }

      List<Number> precipitations = (List<Number>) hourly.get("precipitation");
      if (precipitations == null || precipitations.isEmpty()) {
        return null;
      }

      double maxPrecip6h = 0.0;
      boolean rainWithin6h = false;
      int hoursToCheck = Math.min(6, precipitations.size());

      for (int i = 0; i < hoursToCheck; i++) {
        double mm = precipitations.get(i).doubleValue();
        if (mm > 0.1) {
          rainWithin6h = true;
        }
        maxPrecip6h = Math.max(maxPrecip6h, mm);
      }

      return new RainForecast(maxPrecip6h, rainWithin6h);
    } catch (Exception e) {
      return null;
    }
  }

  private RainForecast fromAlertsForForest(String forestId) {
    LocalDateTime now = ParisTime.now();
    LocalDateTime in6h = now.plus(6, ChronoUnit.HOURS);

    List<WeatherAlert> alerts = weatherAlertRepository.findAll().stream()
        .filter(a -> !a.isAcknowledged())
        .filter(a -> RAIN_ALERT_TYPES.contains(a.getType()))
        .filter(a -> a.getTimestamp() != null
            && !a.getTimestamp().isBefore(now)
            && !a.getTimestamp().isAfter(in6h))
        .toList();

    if (alerts.isEmpty()) {
      return new RainForecast(0.0, false);
    }

    double maxPrecip = alerts.stream()
        .mapToDouble(this::extractPrecipitation)
        .max()
        .orElse(10.0);

    return new RainForecast(maxPrecip, true);
  }

  private RainForecast fromAlertsOnly() {
    LocalDateTime now = ParisTime.now();
    LocalDateTime in6h = now.plus(6, ChronoUnit.HOURS);

    boolean rainSoon = weatherAlertRepository.findAll().stream()
        .filter(a -> !a.isAcknowledged())
        .filter(a -> RAIN_ALERT_TYPES.contains(a.getType()))
        .anyMatch(a -> a.getTimestamp() != null
            && !a.getTimestamp().isBefore(now)
            && !a.getTimestamp().isAfter(in6h));

    return new RainForecast(rainSoon ? 15.0 : 0.0, rainSoon);
  }

  private double extractPrecipitation(WeatherAlert alert) {
    if (alert.getDetails() == null) {
      return 10.0;
    }
    Object precip = alert.getDetails().get("precipitation");
    if (precip instanceof Number number) {
      return number.doubleValue();
    }
    return 10.0;
  }
}
