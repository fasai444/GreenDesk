package org.example.entities.alert;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PlantAlertTest {

    @Test
    void constructors_and_getters_setters_shouldWork() {
        LocalDateTime ts = LocalDateTime.now();
        PlantAlert alert = new PlantAlert("plant-1", ts, AlertType.HIGH_LIGHT, AlertSeverity.WARNING, "msg");

        assertEquals("plant-1", alert.getPlantId());
        assertEquals(ts, alert.getCreatedAt());
        assertEquals(AlertType.HIGH_LIGHT, alert.getType());
        assertEquals(AlertSeverity.WARNING, alert.getSeverity());
        assertEquals("msg", alert.getMessage());
        assertFalse(alert.isAcknowledged());

        alert.setId("a1");
        alert.setPlantId("plant-2");
        alert.setCreatedAt(ts.plusHours(1));
        alert.setType(AlertType.LOW_WATER);
        alert.setSeverity(AlertSeverity.CRITICAL);
        alert.setMessage("updated");
        alert.setAcknowledged(true);

        assertEquals("a1", alert.getId());
        assertEquals("plant-2", alert.getPlantId());
        assertEquals(ts.plusHours(1), alert.getCreatedAt());
        assertEquals(AlertType.LOW_WATER, alert.getType());
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
        assertEquals("updated", alert.getMessage());
        assertTrue(alert.isAcknowledged());
    }
}
