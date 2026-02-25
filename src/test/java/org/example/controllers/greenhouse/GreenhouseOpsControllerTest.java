package org.example.controllers.greenhouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.services.GreenhouseOpsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreenhouseOpsController.class)
class GreenhouseOpsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GreenhouseOpsService greenhouseOpsService;

    @Test
    void getOverview_success() throws Exception {
        when(greenhouseOpsService.getOverview()).thenReturn(Map.of("plants", 12, "forests", 2));

        mockMvc.perform(get("/api/greenhouse/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plants").value(12))
                .andExpect(jsonPath("$.forests").value(2));
    }

    @Test
    void getOverview_error() throws Exception {
        when(greenhouseOpsService.getOverview()).thenThrow(new RuntimeException("overview failure"));

        mockMvc.perform(get("/api/greenhouse/overview"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("overview failure"))
                .andExpect(jsonPath("$.endpoint").value("/overview"));
    }

    @Test
    void getLiveEffectsAndRoiForests_success() throws Exception {
        when(greenhouseOpsService.getLiveEffectsImpact(eq(5))).thenReturn(List.of(Map.of("plantId", "p-1")));
        when(greenhouseOpsService.getForestRoiRanking(eq(8), eq(168))).thenReturn(List.of(Map.of("level", "STABLE")));

        mockMvc.perform(get("/api/greenhouse/live-effects").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].plantId").value("p-1"));

        mockMvc.perform(get("/api/greenhouse/roi/forests").param("limit", "8").param("hours", "168"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].level").value("STABLE"));
    }

    @Test
    void getRoiAndAlerts_error() throws Exception {
        when(greenhouseOpsService.getRoiInsights(anyInt())).thenThrow(new RuntimeException("roi failure"));
        when(greenhouseOpsService.getActiveAlerts(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("alerts failure"));

        mockMvc.perform(get("/api/greenhouse/roi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("roi failure"))
                .andExpect(jsonPath("$.endpoint").value("/roi"));

        mockMvc.perform(get("/api/greenhouse/alerts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("alerts failure"))
                .andExpect(jsonPath("$.endpoint").value("/alerts"));
    }

    @Test
    void emitSensorTick_shouldValidateForestId() throws Exception {
        mockMvc.perform(post("/api/greenhouse/sensor-stream/tick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("profile", "NORMAL"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("forestId est requis"))
                .andExpect(jsonPath("$.endpoint").value("/sensor-stream/tick"));
    }

    @Test
    void emitSensorTick_successAndError() throws Exception {
        when(greenhouseOpsService.emitSensorTick(eq("f-1"), eq("HOT_DRY")))
                .thenReturn(Map.of("createdReadings", 3, "failedReadings", 0));

        mockMvc.perform(post("/api/greenhouse/sensor-stream/tick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("forestId", "f-1", "profile", "HOT_DRY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdReadings").value(3));

        when(greenhouseOpsService.emitSensorTick(eq("f-2"), eq("NORMAL")))
                .thenThrow(new RuntimeException("tick failure"));

        mockMvc.perform(post("/api/greenhouse/sensor-stream/tick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("forestId", "f-2", "profile", "NORMAL"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("tick failure"))
                .andExpect(jsonPath("$.endpoint").value("/sensor-stream/tick"));
    }

    @Test
    void emitSensorTick_shouldNormalizeProfileAndForestId() throws Exception {
        when(greenhouseOpsService.emitSensorTick(eq("f-3"), eq("NORMAL")))
                .thenReturn(Map.of("createdReadings", 1, "failedReadings", 0));

        mockMvc.perform(post("/api/greenhouse/sensor-stream/tick")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("forestId", " f-3 ", "profile", "  normal  "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdReadings").value(1));
    }

    @Test
    void endpoints_shouldClampLimitAndHoursBounds() throws Exception {
        when(greenhouseOpsService.getLiveEffectsImpact(eq(1))).thenReturn(List.of());
        when(greenhouseOpsService.getForestRoiRanking(eq(500), eq(720))).thenReturn(List.of());

        mockMvc.perform(get("/api/greenhouse/live-effects").param("limit", "-4"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(get("/api/greenhouse/roi/forests")
                .param("limit", "999")
                .param("hours", "1200"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(greenhouseOpsService).getLiveEffectsImpact(1);
        verify(greenhouseOpsService).getForestRoiRanking(500, 720);
    }
}
