package org.example.controllers.alert;

import org.example.entities.alert.AlertSeverity;
import org.example.entities.alert.AlertType;
import org.example.entities.alert.PlantAlert;
import org.example.services.PlantAlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.example.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlantAlertController.class)
@Import(TestSecurityConfig.class)
class PlantAlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantAlertService plantAlertService;

    @Test
    void listAlerts_success() throws Exception {
        PlantAlert alert = new PlantAlert("p1", LocalDateTime.now(), AlertType.HIGH_TEMPERATURE, AlertSeverity.WARNING,
                "Temp high");
        alert.setId("a1");

        when(plantAlertService.getAlertsForPlant(eq("p1"), anyBoolean())).thenReturn(List.of(alert));

        mockMvc.perform(get("/plants/p1/alerts").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("a1"))
                .andExpect(jsonPath("$[0].plantId").value("p1"));
    }

    @Test
    void listAlerts_error() throws Exception {
        when(plantAlertService.getAlertsForPlant(eq("p1"), anyBoolean())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/plants/p1/alerts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("boom"));
    }

    @Test
    void acknowledge_success_and_error() throws Exception {
        PlantAlert ack = new PlantAlert("p1", LocalDateTime.now(), AlertType.LOW_WATER, AlertSeverity.CRITICAL,
                "Water low");
        ack.setId("a2");
        ack.setAcknowledged(true);

        when(plantAlertService.acknowledge("a2")).thenReturn(ack);
        when(plantAlertService.acknowledge("missing")).thenThrow(new Exception("Alerte introuvable"));

        mockMvc.perform(post("/alerts/a2/ack"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("a2"))
                .andExpect(jsonPath("$.acknowledged").value(true));

        mockMvc.perform(post("/alerts/missing/ack"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Alerte introuvable"));
    }
}
