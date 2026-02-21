package org.example.controllers.plant;

import org.example.entites.plant.Plant;
import org.example.entites.plant.PlantState;
import org.example.services.PlantServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantController.class)
class PlantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlantServices plantServices;

    private Plant mockPlant;

    @BeforeEach
    void setUp() {
        mockPlant = new Plant();
        mockPlant.setName("Rose");
        mockPlant.setWaterLevel(50.0);
        mockPlant.setTemperature(20.0);
        mockPlant.setHumidity(40.0);
        mockPlant.setLux(1000.0);
        mockPlant.setPlantState(PlantState.HEALTHY);
        mockPlant.setStressIndex(0.1);
        mockPlant.setHeightCm(15.0);
        mockPlant.setForestId("forest-1");
    }

    @Test
    void testCreatePlantWithAllParams_Success() throws Exception {
        when(plantServices.createPlant(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(mockPlant);

        mockMvc.perform(post("/plants/create")
                .param("name", "Rose")
                .param("speciesId", "spec-1")
                .param("water", "50.0")
                .param("temperature", "20.0")
                .param("humidity", "40.0")
                .param("lux", "1000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rose"));
    }

    @Test
    void testCreatePlantWithMinimalParams_Success() throws Exception {
        when(plantServices.createPlant("Rose", "spec-1")).thenReturn(mockPlant);

        mockMvc.perform(post("/plants/create")
                .param("name", "Rose")
                .param("speciesId", "spec-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rose"));
    }

    @Test
    void testCreatePlant_BadRequest() throws Exception {
        when(plantServices.createPlant(anyString(), anyString())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/plants/create")
                .param("name", "Rose")
                .param("speciesId", "spec-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllPlants() throws Exception {
        when(plantServices.getAllPlants()).thenReturn(Arrays.asList(mockPlant));

        mockMvc.perform(get("/plants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Rose"));
    }

    @Test
    void testGetPlantById_Found() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.of(mockPlant));

        mockMvc.perform(get("/plants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rose"));
    }

    @Test
    void testGetPlantById_NotFound() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/plants/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetPlantState_Found() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.of(mockPlant));

        mockMvc.perform(get("/plants/1/state"))
                .andExpect(status().isOk())
                .andExpect(content().string("HEALTHY"));
    }

    @Test
    void testGetPlantState_NotFound() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/plants/1/state"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDetailedStatus_Found() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.of(mockPlant));

        mockMvc.perform(get("/plants/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rose"))
                .andExpect(jsonPath("$.forestId").value("forest-1"))
                .andExpect(jsonPath("$.state").value("HEALTHY"))
                .andExpect(jsonPath("$.sensors.temperature").value(20.0));
    }

    @Test
    void testGetDetailedStatus_NotFound() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/plants/1/status"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePlant_Success() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.of(mockPlant));
        
        // Mocking the evaluateState method to avoid NullPointerException on missing species
        Plant spyPlant = spy(mockPlant);
        doReturn(PlantState.STRESSED).when(spyPlant).evaluateState();
        when(plantServices.getPlantById("1")).thenReturn(Optional.of(spyPlant));

        mockMvc.perform(put("/plants/1")
                .param("water", "10.0")
                .param("temperature", "35.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waterLevel").value(10.0))
                .andExpect(jsonPath("$.temperature").value(35.0));
    }

    @Test
    void testUpdatePlant_NotFound() throws Exception {
        when(plantServices.getPlantById("1")).thenReturn(Optional.empty());

        mockMvc.perform(put("/plants/1")
                .param("water", "10.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePlant_Success() throws Exception {
        doNothing().when(plantServices).deletePlantById("1");

        mockMvc.perform(delete("/plants/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Plante supprimée avec succès"));
    }

    @Test
    void testDeleteAllPlants() throws Exception {
        doNothing().when(plantServices).deleteAllPlants();

        mockMvc.perform(delete("/plants"))
                .andExpect(status().isOk())
                .andExpect(content().string("Toutes les plantes ont été supprimées"));
    }
}