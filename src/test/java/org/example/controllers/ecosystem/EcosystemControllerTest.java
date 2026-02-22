package org.example.controllers.ecosystem;

import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;
import org.example.entites.species.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("EcosystemController – simulation & état")
class EcosystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private SpeciesRepository speciesRepository;

    private void ensureForestWithOneCell() {
        boolean hasCellData = forestRepository.findAll().stream().anyMatch(f -> !f.getCells().isEmpty());
        if (hasCellData) {
            return;
        }

        forestRepository.deleteAll();
        plantRepository.deleteAll();
        speciesRepository.deleteAll();

        Species species = new Species("Tomate", 220, 24, 65, 3000, 1.5, 0.4);
        species = speciesRepository.save(species);

        Plant plant = new Plant("Plant-1", species);
        plant = plantRepository.save(plant);

        Forest forest = new Forest("Forêt test", 5, 5);
        forest.addCell(new Forest.ForestCell(0, 0, plant.getId()));
        forestRepository.save(forest);
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/ecosystem/cells → initialise + retourne liste")
    void getCellsStatus_initializes_and_returns_list() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(get("/api/ecosystem/cells"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0]", containsString("Cell [")))
                .andExpect(jsonPath("$[0]", containsString("Plant:")));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/ecosystem/tick → effectue un tick")
    void tick_performs_one_step() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(post("/api/ecosystem/tick"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tick effectué")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/ecosystem/simulate/3 → simule plusieurs ticks")
    void simulate_multiple_ticks() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(post("/api/ecosystem/simulate/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("3 ticks simulés")));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/ecosystem/cells après ticks → format cohérent")
    void cells_after_ticks_still_valid_format() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(get("/api/ecosystem/cells"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", everyItem(containsString("Cell"))))
                .andExpect(jsonPath("$[*]", everyItem(containsString("Plant:"))));
    }

    @Test
    @Order(5)
    @DisplayName("simulate avec n=0 → message correct")
    void simulate_zero_ticks() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(post("/api/ecosystem/simulate/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0 ticks simulés")));
    }

    @Test
    @Order(6)
    @DisplayName("simulate n négatif → géré")
    void simulate_negative_should_be_handled() throws Exception {
        ensureForestWithOneCell();

        mockMvc.perform(post("/api/ecosystem/simulate/-2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ticks simulés")));
    }
}
