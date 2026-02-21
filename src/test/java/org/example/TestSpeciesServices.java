package org.example;

import org.example.entites.species.Species;
import org.example.services.SpeciesServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestSpeciesServices {

    @Autowired
    private SpeciesServices speciesServices;

    @Test
    void createSpecies_shouldFailIfNameAlreadyExists() throws Exception {
        Species species;
        // Vérifie si l'espèce existe déjà
        species = speciesServices.getSpeciesByName("Lavande")
                .orElseGet(() -> {
                    try {
                        return speciesServices.createSpecies(new Species("Lavande"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        assertNotNull(species.getId(), "L'espèce Lavande doit avoir un ID");

        // Essai de créer la même espèce => exception attendue
        Exception ex = assertThrows(Exception.class, () ->
                speciesServices.createSpecies(new Species("Lavande"))
        );
        assertTrue(ex.getMessage().contains("existe déjà"), "La création d'un doublon doit échouer");
    }


    @Test
    void getSpeciesByName_success() throws Exception {
        Species species;
        // Vérifie si l'espèce existe déjà
        if (speciesServices.getSpeciesByName("Basilic").isPresent()) {
            species = speciesServices.getSpeciesByName("Basilic").get();
        } else {
            species = speciesServices.createSpecies(new Species("Basilic"));
        }

        Species s = speciesServices.getSpeciesByName("Basilic")
                .orElseThrow();

        assertEquals("Basilic", s.getName());
    }

}
