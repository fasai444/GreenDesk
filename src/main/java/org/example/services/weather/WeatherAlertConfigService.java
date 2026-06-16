package org.example.services.weather;

import org.example.entities.forest.Forest;
import org.example.repositories.ForestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WeatherAlertConfigService {

    @Value("${tomorrow.api.key:}")
    private String tomorrowApiKey;

    @Value("${tomorrow.webhook.url:http://localhost:8080/api/weather/webhook}")
    private String webhookUrl;

    @Autowired
    private ForestRepository forestRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Configure les alertes Tomorrow.io pour une forêt donnée.
     * Envoie une requête POST à Tomorrow.io avec les seuils personnalisés et l'URL du webhook.
     *
     * @param forestId  ID de la forêt
     * @param thresholds Map de seuils personnalisés (ex: {"frost": -2.0, "heatwave": 38.0})
     * @return Map avec le résultat de la configuration
     */
    public Map<String, Object> configurerAlertesPourForet(String forestId, Map<String, Object> thresholds) {
        if (forestId == null || forestId.isBlank()) {
            return Map.of("success", false, "message", "forestId requis");
        }
        Optional<Forest> forestOpt = forestRepository.findById(forestId);
        if (forestOpt.isEmpty()) {
            return Map.of("success", false, "message", "Forêt non trouvée : " + forestId);
        }

        Forest forest = forestOpt.get();
        double[] coords = forest.getCoords();

        if (coords == null || coords.length < 2) {
            return Map.of("success", false, "message", "Coordonnées manquantes pour la forêt " + forestId);
        }

        if (tomorrowApiKey == null || tomorrowApiKey.isBlank()) {
            // Pas de clé API configurée — on simule la configuration
            return Map.of(
                "success", true,
                "simulated", true,
                "message", "Configuration simulée (aucune clé Tomorrow.io configurée)",
                "forestId", forestId,
                "webhookUrl", webhookUrl,
                "thresholds", thresholds != null ? thresholds : defaultThresholds()
            );
        }

        // Appel réel à Tomorrow.io
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", tomorrowApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("name", "GreenDesk-" + forest.getName());
            body.put("webhookUrl", webhookUrl);
            body.put("location", Map.of("lat", coords[0], "lon", coords[1]));
            body.put("thresholds", thresholds != null ? thresholds : defaultThresholds());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            @SuppressWarnings("null")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.tomorrow.io/v4/alerts",
                HttpMethod.POST,
                request,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return Map.of(
                "success", true,
                "tomorrowResponse", response.getBody() != null ? response.getBody() : Map.of(),
                "forestId", forestId
            );
        } catch (Exception e) {
            return Map.of("success", false, "message", "Erreur appel Tomorrow.io : " + e.getMessage());
        }
    }

    /**
     * Seuils par défaut si aucun n'est fourni.
     */
    private Map<String, Object> defaultThresholds() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("frost", Map.of("temperature", 0.0, "unit", "°C"));
        defaults.put("heatwave", Map.of("temperature", 35.0, "unit", "°C"));
        defaults.put("heavy_rain", Map.of("precipitation", 50.0, "unit", "mm/h"));
        defaults.put("high_wind", Map.of("windSpeed", 60.0, "unit", "km/h"));
        defaults.put("uv_alert", Map.of("uvIndex", 8.0, "unit", "UVI"));
        return defaults;
    }
}
