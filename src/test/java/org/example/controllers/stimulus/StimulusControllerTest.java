package org.example.controllers.stimulus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entities.Stimulus;
import org.example.services.StimulusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StimulusController.class)
class StimulusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StimulusService stimulusService;

    @Test
    void createStimulus_success() throws Exception {
        Stimulus stimulus = new Stimulus("HEATWAVE", "forest-1", 4.0, 6);

        when(stimulusService.applyToForest(any(Stimulus.class))).thenReturn(stimulus);

        mockMvc.perform(post("/api/stimuli")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stimulus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("HEATWAVE"))
                .andExpect(jsonPath("$.forestId").value("forest-1"));
    }

    @Test
    void createStimulus_badRequest() throws Exception {
        Stimulus stimulus = new Stimulus("", "forest-1", 4.0, 0);

        when(stimulusService.applyToForest(any(Stimulus.class)))
                .thenThrow(new IllegalArgumentException("durationHours doit être > 0"));

        mockMvc.perform(post("/api/stimuli")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stimulus)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("durationHours doit être > 0"));
    }
}
