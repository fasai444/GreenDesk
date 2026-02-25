package org.example.services;

import org.example.entities.alert.AlertSeverity;
import org.example.entities.alert.AlertType;
import org.example.entities.alert.PlantAlert;
import org.example.entities.plant.Plant;
import org.example.entities.species.Species;
import org.example.repositories.PlantAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PlantAlertService {

    // Anti-spam: ne pas recréer la même alerte plus d'une fois toutes les 30
    // minutes
    private static final int DEDUP_MINUTES = 30;

    @Autowired
    private PlantAlertRepository plantAlertRepository;

    public List<PlantAlert> getAlertsForPlant(String plantId, boolean activeOnly) {
        String safePlantId = Objects.requireNonNull(plantId, "plantId must not be null");
        if (activeOnly) {
            return plantAlertRepository.findByPlantIdAndAcknowledgedFalseOrderByCreatedAtDesc(safePlantId);
        }
        return plantAlertRepository.findByPlantIdOrderByCreatedAtDesc(safePlantId);
    }

    public PlantAlert acknowledge(String alertId) throws Exception {
        String safeAlertId = Objects.requireNonNull(alertId, "alertId must not be null");
        PlantAlert alert = plantAlertRepository.findById(safeAlertId)
                .orElseThrow(() -> new Exception("Alerte introuvable: " + safeAlertId));
        alert.setAcknowledged(true);
        return plantAlertRepository.save(alert);
    }

    /**
     * Appelé après chaque nouvelle lecture capteur.
     * Compare reading + plant current state vs species optimals et crée des
     * alertes.
     */
    public List<PlantAlert> evaluateAndCreateAlerts(Plant plant) {
        Species s = plant.getSpecies();
        List<PlantAlert> created = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // --- Temperature ---
        double tempDiff = plant.getTemperature() - s.getOptimalTemperature();
        if (Math.abs(tempDiff) > 4) {
            AlertSeverity sev = Math.abs(tempDiff) > 8 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            AlertType type = tempDiff < 0 ? AlertType.LOW_TEMPERATURE : AlertType.HIGH_TEMPERATURE;
            String msg = String.format("Température %.1f°C (optimal %.1f°C) — écart %.1f°C",
                    plant.getTemperature(), s.getOptimalTemperature(), tempDiff);
            maybeCreate(created, plant.getId(), now, type, sev, msg);
        }

        // --- Humidity ---
        double humDiff = plant.getHumidity() - s.getOptimalHumidity();
        if (Math.abs(humDiff) > 10) {
            AlertSeverity sev = Math.abs(humDiff) > 20 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            AlertType type = humDiff < 0 ? AlertType.LOW_HUMIDITY : AlertType.HIGH_HUMIDITY;
            String msg = String.format("Humidité %.1f%% (optimal %.1f%%) — écart %.1f%%",
                    plant.getHumidity(), s.getOptimalHumidity(), humDiff);
            maybeCreate(created, plant.getId(), now, type, sev, msg);
        }

        // --- Light (Lux) ---
        double optimalLux = s.getOptimalLuxNeeds();
        if (plant.getLux() < 0.7 * optimalLux) {
            AlertSeverity sev = plant.getLux() < 0.5 * optimalLux ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            String msg = String.format("Lumière %.0f lx (optimal %.0f lx) — trop faible",
                    plant.getLux(), optimalLux);
            maybeCreate(created, plant.getId(), now, AlertType.LOW_LIGHT, sev, msg);
        } else if (plant.getLux() > 1.3 * optimalLux) {
            AlertSeverity sev = plant.getLux() > 1.6 * optimalLux ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            String msg = String.format("Lumière %.0f lx (optimal %.0f lx) — trop forte",
                    plant.getLux(), optimalLux);
            maybeCreate(created, plant.getId(), now, AlertType.HIGH_LIGHT, sev, msg);
        }

        // --- Water level ---
        double waterDiff = plant.getWaterLevel() - s.getOptimalWaterNeeds();
        if (Math.abs(waterDiff) > 15) {
            AlertSeverity sev = Math.abs(waterDiff) > 30 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
            AlertType type = waterDiff < 0 ? AlertType.LOW_WATER : AlertType.HIGH_WATER;
            String msg = String.format("Eau %.1f mL (optimal %.1f mL) — écart %.1f mL",
                    plant.getWaterLevel(), s.getOptimalWaterNeeds(), waterDiff);
            maybeCreate(created, plant.getId(), now, type, sev, msg);
        }

        return created;
    }

    private void maybeCreate(List<PlantAlert> created, String plantId, LocalDateTime now,
            AlertType type, AlertSeverity severity, String message) {

        Optional<PlantAlert> last = plantAlertRepository
                .findFirstByPlantIdAndTypeAndAcknowledgedFalseOrderByCreatedAtDesc(plantId, type);

        if (last.isPresent()) {
            LocalDateTime limit = now.minusMinutes(DEDUP_MINUTES);
            if (last.get().getCreatedAt() != null && last.get().getCreatedAt().isAfter(limit)) {
                return; // anti-spam: ignore
            }
        }

        PlantAlert alert = new PlantAlert(plantId, now, type, severity, message);
        PlantAlert saved = plantAlertRepository.save(alert);
        created.add(saved);
    }
}