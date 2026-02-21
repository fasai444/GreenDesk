package org.example.controllers.environment;

import org.example.entites.environment.Season;
import org.example.entites.environment.SeasonType;
import org.example.services.SeasonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeasonController.class)
class SeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeasonService seasonService;

    @Test
    void testGetAllSeasons() throws Exception {
        // Étant donné (Given)
        Season winter = new Season(SeasonType.WINTER, "Hiver", -8.0, -15.0, -2000.0, -30.0);
        Season spring = new Season(SeasonType.SPRING, "Printemps", 5.0, 10.0, 1000.0, 20.0);
        List<Season> seasons = Arrays.asList(winter, spring);

        when(seasonService.getAllSeasons()).thenReturn(seasons);

        // Quand (When) & Alors (Then)
        mockMvc.perform(get("/api/seasons")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Hiver"))
                .andExpect(jsonPath("$[1].name").value("Printemps"));
    }
}