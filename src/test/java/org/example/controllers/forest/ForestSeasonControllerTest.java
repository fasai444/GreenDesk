package org.example.controllers.forest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.environment.Season;
import org.example.entities.environment.SeasonCycle;
import org.example.entities.environment.SeasonType;
import org.example.services.SeasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.example.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForestSeasonController.class)
@Import(TestSecurityConfig.class)
class ForestSeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeasonService seasonService;

    private SeasonCycle mockCycle;

    @BeforeEach
    void setUp() {
        mockCycle = new SeasonCycle("f1");
        mockCycle.setCurrentSeason(SeasonType.SPRING);
    }

    @Test
    void testCreateSeasonCycle_Success() throws Exception {
        when(seasonService.createSeasonCycle("f1")).thenReturn(mockCycle);

        mockMvc.perform(post("/api/forests/f1/season-cycle"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.forestId").value("f1"))
                .andExpect(jsonPath("$.currentSeason").value("SPRING"));
    }

    @Test
    void testCreateSeasonCycle_BadRequest() throws Exception {
        when(seasonService.createSeasonCycle(anyString()))
                .thenThrow(new RuntimeException("Forêt inexistante"));

        mockMvc.perform(post("/api/forests/f1/season-cycle"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Forêt inexistante"));
    }

    @Test
    void testGetSeasonCycle_Found() throws Exception {
        when(seasonService.getSeasonCycle("f1")).thenReturn(Optional.of(mockCycle));
        
        // Le contrôleur enrichit la réponse avec "currentSeasonData". 
        // L'objet renvoyé doit donc avoir une clé "cycle" et une clé "currentSeasonData".
        mockMvc.perform(get("/api/forests/f1/season-cycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycle.forestId").value("f1"))
                .andExpect(jsonPath("$.currentSeasonData").exists())
                .andExpect(jsonPath("$.currentSeasonData.name").value("Printemps"));
    }

    @Test
    void testGetSeasonCycle_NotFound() throws Exception {
        when(seasonService.getSeasonCycle("f1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forests/f1/season-cycle"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testAdvanceSeasonCycle_Success() throws Exception {
        Map<String, Object> request = Map.of("monthsElapsed", 3);
        
        // Si on avance de 3 mois, on devrait passer à l'été
        mockCycle.setCurrentSeason(SeasonType.SUMMER); 
        when(seasonService.advanceSeasonCycle("f1", 3)).thenReturn(mockCycle);

        mockMvc.perform(post("/api/forests/f1/season-cycle/advance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cycle.currentSeason").value("SUMMER"))
                .andExpect(jsonPath("$.currentSeasonData.name").value("Été"));
    }

    @Test
    void testAdvanceSeasonCycle_NotFound() throws Exception {
        Map<String, Object> request = Map.of("monthsElapsed", 1);
        
        when(seasonService.advanceSeasonCycle(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Cycle non trouvé"));

        mockMvc.perform(post("/api/forests/f1/season-cycle/advance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Cycle non trouvé"));
    }

    @Test
    void testDeleteSeasonCycle() throws Exception {
        doNothing().when(seasonService).deleteSeasonCycle("f1");

        mockMvc.perform(delete("/api/forests/f1/season-cycle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cycle de saisons supprimé"));
                
        verify(seasonService, times(1)).deleteSeasonCycle("f1");
    }
}