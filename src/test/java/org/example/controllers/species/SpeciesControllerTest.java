package org.example.controllers.species;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.species.Species;
import org.example.services.SpeciesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpeciesController.class)
class SpeciesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpeciesService speciesServices;

    private Species mockSpecies;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        mockSpecies = new Species();
        mockSpecies.setName("Oak");
    }

    @Test
    void testGetAllSpecies() throws Exception {
        when(speciesServices.getAllSpecies()).thenReturn(Arrays.asList(mockSpecies));

        mockMvc.perform(get("/api/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Oak"));
    }

    @Test
    void testGetSpeciesByName_Found() throws Exception {
        when(speciesServices.getSpeciesByName("Oak")).thenReturn(Optional.of(mockSpecies));

        mockMvc.perform(get("/api/species/Oak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oak"));
    }

    @Test
    void testGetSpeciesByName_NotFound() throws Exception {
        when(speciesServices.getSpeciesByName("Oak")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/species/Oak"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSpecies_Success() throws Exception {
        when(speciesServices.createSpecies(any(Species.class))).thenReturn(mockSpecies);

        mockMvc.perform(post("/api/species")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(mockSpecies))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oak"));
    }

    @Test
    void testCreateSpecies_BadRequest() throws Exception {
        when(speciesServices.createSpecies(any(Species.class))).thenThrow(new RuntimeException("Invalid Data"));

        mockMvc.perform(post("/api/species")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(mockSpecies))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Data"));
    }

    @Test
    void testUpdateSpecies_Success() throws Exception {
        doNothing().when(speciesServices).deleteSpeciesById("1");
        when(speciesServices.createSpecies(any(Species.class))).thenReturn(mockSpecies);

        mockMvc.perform(put("/api/species/1")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(mockSpecies))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Oak"));
    }

    @Test
    void testUpdateSpecies_BadRequest() throws Exception {
        doThrow(new RuntimeException("ID not found")).when(speciesServices).deleteSpeciesById("1");

        mockMvc.perform(put("/api/species/1")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(mockSpecies))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ID not found"));
    }

    @Test
    void testDeleteSpecies_Success() throws Exception {
        doNothing().when(speciesServices).deleteSpeciesById("1");

        mockMvc.perform(delete("/api/species/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Espèce supprimée avec succès"));
    }

    @Test
    void testDeleteSpecies_BadRequest() throws Exception {
        doThrow(new RuntimeException("Deletion failed")).when(speciesServices).deleteSpeciesById("1");

        mockMvc.perform(delete("/api/species/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deletion failed"));
    }

    @Test
    void testDeleteAllSpecies() throws Exception {
        doNothing().when(speciesServices).deleteAllSpecies();

        mockMvc.perform(delete("/api/species"))
                .andExpect(status().isOk())
                .andExpect(content().string("Toutes les espèces ont été supprimées"));
    }
}