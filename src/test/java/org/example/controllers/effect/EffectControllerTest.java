package org.example.controllers.effect;

import com.jayway.jsonpath.JsonPath;
import org.example.entites.plant.Plant;
import org.example.entites.species.Species;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.example.services.EffectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("EffectController – gestion des effets")
class EffectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EffectService effectService;

    @Autowired
    private EffectRepository effectRepository;

    @Autowired
    private PlantEffectRepository plantEffectRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    private String plantId;

    @BeforeEach
    void setup() {
        plantEffectRepository.deleteAll();
        effectRepository.deleteAll();
        plantRepository.deleteAll();
        speciesRepository.deleteAll();

        Species species = new Species("TestSpecies", 200.0, 22.0, 60.0, 1500.0, 1.0, 0.4);
        species = speciesRepository.save(species);

        Plant plant = new Plant("TestPlant", species);
        plant = plantRepository.save(plant);
        plantId = plant.getId();

        effectService.initializeEffectsCatalog();
    }

    @Test
    @DisplayName("GET /api/effects → retourne catalogue")
    void getAllEffects_returns_catalog() throws Exception {
        mockMvc.perform(get("/api/effects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].name", hasItems("Shade", "Fertilizer", "Heating")));
    }

    @Test
    @DisplayName("GET /api/effects?custom=true → endpoint répond")
    void getCustomEffects_endpoint_responds() throws Exception {
        mockMvc.perform(get("/api/effects").param("custom", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/effects → crée effet custom")
    void createCustomEffect_success() throws Exception {
        String payload = """
                {
                  "name": "SuperBoost",
                  "description": "Effet test L3",
                  "durationHours": 24,
                  "growthRateModifier": 1.2
                }
                """;

        mockMvc.perform(post("/api/effects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SuperBoost"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    @DisplayName("POST /api/plants/{plantId}/effects/{effectId} → applique effet")
    void applyEffectToPlant_success() throws Exception {
        String effectId = effectRepository.findAll().stream()
                .filter(e -> "Shade".equals(e.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(post("/api/plants/{plantId}/effects/{effectId}", plantId, effectId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plantId").value(plantId))
                .andExpect(jsonPath("$.effectId").value(effectId));
    }

    @Test
    @DisplayName("GET /api/plants/{plantId}/effects → liste effets appliqués")
    void getPlantEffects_returns_list() throws Exception {
        mockMvc.perform(get("/api/plants/{plantId}/effects", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/plants/{plantId}/effects/active → effets non expirés")
    void getActiveEffects() throws Exception {
        String effectId = effectRepository.findAll().stream()
                .filter(e -> "Shade".equals(e.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        mockMvc.perform(post("/api/plants/{plantId}/effects/{effectId}", plantId, effectId))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/plants/{plantId}/effects/active", plantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].active", everyItem(is(true))));
    }

    @Test
    @DisplayName("DELETE /api/plants/effects/{plantEffectId} → supprime effet")
    void removeEffect_success() throws Exception {
        String effectId = effectRepository.findAll().stream()
                .filter(e -> "Shade".equals(e.getName()))
                .findFirst()
                .orElseThrow()
                .getId();

        String body = mockMvc.perform(post("/api/plants/{plantId}/effects/{effectId}", plantId, effectId))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String plantEffectId = JsonPath.read(body, "$.id");

        mockMvc.perform(delete("/api/plants/effects/{plantEffectId}", plantEffectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("succès")));
    }
}
