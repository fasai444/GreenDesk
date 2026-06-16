package org.example.controllers.forest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.forest.Forest;
import org.example.entities.plant.Plant;
import org.example.services.ForestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.example.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForestController.class)
@Import(TestSecurityConfig.class)
class ForestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForestService forestService;

    private Forest mockForest;

    @BeforeEach
    void setUp() {
        mockForest = new Forest("Forêt Magique", 10, 10);
    }

    @Test
    void testCreateForest_Success() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Forêt Magique");
        request.put("width", 10);
        request.put("height", 10);

        when(forestService.createForest("Forêt Magique", 10, 10)).thenReturn(mockForest);

        mockMvc.perform(post("/api/forests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Forêt Magique"))
                .andExpect(jsonPath("$.width").value(10));
    }

    @Test
    void testCreateForest_BadRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Forêt Magique");
        request.put("width", 0);
        request.put("height", 0);

        when(forestService.createForest(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Dimensions invalides"));

        mockMvc.perform(post("/api/forests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dimensions invalides"));
    }

    @Test
    void testGetAllForests() throws Exception {
        when(forestService.getAllForests()).thenReturn(Arrays.asList(mockForest));

        mockMvc.perform(get("/api/forests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetForestById_Found() throws Exception {
        when(forestService.getForestById("f1")).thenReturn(Optional.of(mockForest));

        mockMvc.perform(get("/api/forests/f1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Forêt Magique"));
    }

    @Test
    void testGetForestById_NotFound() throws Exception {
        when(forestService.getForestById("f1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forests/f1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Forêt introuvable"));
    }

    @Test
    void testAddPlantToForest_Success() throws Exception {
        Map<String, Object> request = Map.of("plantId", "p1", "x", 2, "y", 3);

        when(forestService.addPlantToForest("f1", "p1", 2, 3)).thenReturn(mockForest);

        mockMvc.perform(post("/api/forests/f1/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testAddPlantToForest_Conflict() throws Exception {
        Map<String, Object> request = Map.of("plantId", "p1", "x", 2, "y", 3);

        when(forestService.addPlantToForest("f1", "p1", 2, 3))
                .thenThrow(new IllegalArgumentException("Position occupée"));

        mockMvc.perform(post("/api/forests/f1/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Position occupée"));
    }

    @Test
    void testAddPlantToForest_Success_WhenCoordinatesAreStrings() throws Exception {
        Map<String, Object> request = Map.of("plantId", "p1", "x", "2", "y", "3");

        when(forestService.addPlantToForest("f1", "p1", 2, 3)).thenReturn(mockForest);

        mockMvc.perform(post("/api/forests/f1/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testAddPlantToForest_BadRequest_WhenPayloadInvalid() throws Exception {
        Map<String, Object> request = Map.of("plantId", "p1", "x", 2.5, "y", 3);

        mockMvc.perform(post("/api/forests/f1/plants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Champ invalide: x"));
    }

    @Test
    void testGetPlantsInForest() throws Exception {
        Plant p1 = new Plant();
        p1.setName("Chêne");
        when(forestService.getPlantsInForest("f1")).thenReturn(Arrays.asList(p1));

        mockMvc.perform(get("/api/forests/f1/plants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Chêne"));
    }

    @Test
    void testRemovePlantFromForest_Success() throws Exception {
        when(forestService.removePlantFromForest("f1", 3, 5)).thenReturn(mockForest);

        mockMvc.perform(delete("/api/forests/f1/plants")
                .param("x", "3")
                .param("y", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteForest_Success() throws Exception {
        doNothing().when(forestService).deleteForest("f1");

        mockMvc.perform(delete("/api/forests/f1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Forêt supprimée avec succès"));
    }
}
