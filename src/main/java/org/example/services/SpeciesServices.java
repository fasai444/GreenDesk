package org.example.services;

import org.example.entites.species.Species;
import org.example.repositories.SpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class SpeciesServices {

    @Autowired
    private SpeciesRepository speciesRepository;

    public Species createSpecies(Species species) throws Exception {
        // Vérifie si le nom existe déjà
        Optional<Species> existing = speciesRepository.findByName(species.getName());
        if (existing.isPresent()) {
            throw new Exception("Une espèce avec ce nom existe déjà : " + species.getName());
        }
        return speciesRepository.save(species);
    }

    public void deleteSpeciesById(String speciesId) {
        speciesRepository.deleteById(speciesId);
    }

    public Optional<Species> getSpeciesByName(String name) {
        return speciesRepository.findByName(name);
    }
    public void deleteAllSpecies() {
        speciesRepository.deleteAll();
    }
    public List<Species> getAllSpecies() {
        return speciesRepository.findAll();
    }
}
