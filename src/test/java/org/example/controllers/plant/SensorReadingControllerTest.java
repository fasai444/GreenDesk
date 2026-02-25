package org.example.controllers.plant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controllers.plant.dto.CreateSensorReadingRequest;
import org.example.entities.SensorReading;
import org.example.services.SensorReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorReadingController.class)
class SensorReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SensorReadingService sensorReadingService;

    @Test
    void createReading_success_and_error() throws Exception {
        SensorReading reading = new SensorReading("p1", LocalDateTime.now(), 22.0, 60.0, 300.0, 5.0);
        reading.setId("r1");

        CreateSensorReadingRequest req = new CreateSensorReadingRequest();
        req.setTemperature(22.0);
        req.setHumidity(60.0);
        req.setLux(300.0);
        req.setRainfall(5.0);

        when(sensorReadingService.addReading(eq("p1"), any(CreateSensorReadingRequest.class))).thenReturn(reading);
        when(sensorReadingService.addReading(eq("missing"), any(CreateSensorReadingRequest.class)))
                .thenThrow(new Exception("Plante introuvable"));

        mockMvc.perform(post("/plants/p1/sensor-readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"));

        mockMvc.perform(post("/plants/missing/sensor-readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Plante introuvable"));
    }

    @Test
    void listReadings_default_and_between_and_error() throws Exception {
        SensorReading r = new SensorReading("p1", LocalDateTime.now(), 20, 50, 100, 1);
        when(sensorReadingService.getReadings("p1")).thenReturn(List.of(r));
        when(sensorReadingService.getReadingsBetween(eq("p1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(r));

        mockMvc.perform(get("/plants/p1/sensor-readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plantId").value("p1"));

        mockMvc.perform(get("/plants/p1/sensor-readings")
                .param("from", "2026-01-01T00:00:00")
                .param("to", "2026-01-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plantId").value("p1"));

        mockMvc.perform(get("/plants/p1/sensor-readings")
                .param("from", "bad")
                .param("to", "also-bad"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void latest_success_and_error() throws Exception {
        SensorReading r = new SensorReading("p1", LocalDateTime.now(), 20, 50, 100, 1);
        when(sensorReadingService.getLatest("p1")).thenReturn(r);
        when(sensorReadingService.getLatest("none")).thenThrow(new Exception("Aucune lecture"));

        mockMvc.perform(get("/plants/p1/sensor-readings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plantId").value("p1"));

        mockMvc.perform(get("/plants/none/sensor-readings/latest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Aucune lecture"));
    }
}
