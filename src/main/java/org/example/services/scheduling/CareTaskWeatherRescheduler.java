package org.example.services.scheduling;

import org.example.entities.care.CareTask;
import org.example.entities.care.CareTaskType;
import org.example.entities.care.TaskStatus;
import org.example.entities.care.WeatherDependency;
import org.example.entities.weather.WeatherAlert;
import org.example.repositories.CareTaskRepository;
import org.example.services.calendar.ExternalCalendarService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Déplace automatiquement les tâches flexibles lors d'alertes météo critiques.
 */
@Service
public class CareTaskWeatherRescheduler {

  private static final Set<String> CRITICAL_ALERT_TYPES = Set.of(
      "heavy_rain", "heatwave", "frost", "high_wind"
  );

  private final CareTaskRepository careTaskRepository;
  private final ExternalCalendarService externalCalendarService;

  public CareTaskWeatherRescheduler(CareTaskRepository careTaskRepository,
                                    ExternalCalendarService externalCalendarService) {
    this.careTaskRepository = careTaskRepository;
    this.externalCalendarService = externalCalendarService;
  }

  public int rescheduleForWeatherAlert(WeatherAlert alert, List<String> forestIds) {
    if (!CRITICAL_ALERT_TYPES.contains(alert.getType())) {
      return 0;
    }

    int rescheduled = 0;
    for (String forestId : forestIds) {
      List<CareTask> flexibleTasks = careTaskRepository
          .findByForestIdAndStatus(forestId, TaskStatus.PENDING)
          .stream()
          .filter(CareTask::isFlexible)
          .toList();

      for (CareTask task : flexibleTasks) {
        if (shouldReschedule(task, alert.getType())) {
          applyReschedule(task, alert.getType());
          careTaskRepository.save(task);
          if (task.getExternalId() != null) {
            externalCalendarService.update(task.getExternalId(), task);
          }
          rescheduled++;
        }
      }
    }
    return rescheduled;
  }

  private boolean shouldReschedule(CareTask task, String alertType) {
    return switch (alertType) {
      case "heavy_rain" -> task.getType() == CareTaskType.WATERING;
      case "heatwave" -> task.getType() == CareTaskType.HEATING_ADJUSTMENT
          || task.getType() == CareTaskType.WATERING;
      case "frost" -> task.getType() == CareTaskType.HEATING_ADJUSTMENT;
      case "high_wind" -> task.getType() == CareTaskType.PRUNING;
      default -> false;
    };
  }

  private void applyReschedule(CareTask task, String alertType) {
    Instant newScheduled = task.getScheduledAt().plus(24, ChronoUnit.HOURS);
    task.setScheduledAt(newScheduled);
    task.setDueAt(newScheduled.plus(4, ChronoUnit.HOURS));

    WeatherDependency dependency = switch (alertType) {
      case "heavy_rain" -> WeatherDependency.RAIN_AVOIDED;
      case "heatwave" -> WeatherDependency.HEAT_ALERT;
      case "frost" -> WeatherDependency.FROST_ALERT;
      default -> task.getWeatherDependency();
    };
    task.setWeatherDependency(dependency);

    task.setDescription(task.getDescription() + " (reporté : alerte " + alertType + ")");
  }
}
