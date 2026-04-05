package org.example;

import org.example.entities.forest.Forest;
import org.example.entities.effect.Effect;
import org.example.entities.plant.Plant;
import org.example.entities.Stimulus;
import org.example.entities.species.Species;
import org.example.repositories.EffectRepository;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.example.repositories.StimulusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final SpeciesRepository speciesRepository;
    private final PlantRepository plantRepository;
    private final ForestRepository forestRepository;
    private final EffectRepository effectRepository;
    private final StimulusRepository stimulusRepository;

    // Injection des dépendances (Les outils pour parler à la BDD)
    public DataInitializer(SpeciesRepository speciesRepository,
            PlantRepository plantRepository,
            ForestRepository forestRepository,
            EffectRepository effectRepository,
            StimulusRepository stimulusRepository) {
        this.speciesRepository = speciesRepository;
        this.plantRepository = plantRepository;
        this.forestRepository = forestRepository;
        this.effectRepository = effectRepository;
        this.stimulusRepository = stimulusRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Démarrage de l'initialisation des données (idempotent)...");
        seedReferenceSpecies();
        seedReferenceEffects();
        seedScenarios();
        System.out.println("Initialisation terminée.");
    }

    private void seedReferenceSpecies() {
        ensureSpecies(new Species("Tomato", 200.0, 22.0, 60.0, 4500.0, 0.65, 0.35));
        ensureSpecies(new Species("Oak", 240.0, 19.0, 58.0, 3800.0, 0.42, 0.22));
        ensureSpecies(new Species("Pine", 170.0, 17.0, 52.0, 3200.0, 0.48, 0.28));
    }

    private void seedReferenceEffects() {
        ensureEffect(Effect.createShadeEffect());
        ensureEffect(Effect.createFertilizerEffect());
        ensureEffect(Effect.createWateringEffect());
        ensureEffect(Effect.createHeatingEffect());
    }

    private void seedScenarios() {
        Species tomato = speciesRepository.findByName("Tomato").orElseThrow();
        Species oak = speciesRepository.findByName("Oak").orElseThrow();
        Species pine = speciesRepository.findByName("Pine").orElseThrow();

        Forest baseline = ensureForest("SCN-01 Baseline Jardin", 8, 8, 48.8566, 2.3522);
        Forest heatwave = ensureForest("SCN-02 Canicule", 10, 10, 48.8600, 2.3500);
        Forest rain = ensureForest("SCN-03 Pluie Intense", 10, 10, 48.8500, 2.3400);
        Forest drought = ensureForest("SCN-04 Secheresse", 10, 10, 48.8600, 2.3600);
        Forest mixed = ensureForest("SCN-05 Mixte Controle", 12, 8, 48.8550, 2.3600);

        ensurePlantInForest("Baseline-Tomato-A", tomato, baseline, 2, 2);
        ensurePlantInForest("Baseline-Oak-B", oak, baseline, 5, 3);
        ensurePlantInForest("Baseline-Pine-C", pine, baseline, 4, 5);

        ensurePlantInForest("Heat-Pine-A", pine, heatwave, 1, 1);
        ensurePlantInForest("Heat-Tomato-B", tomato, heatwave, 7, 4);
        ensurePlantInForest("Heat-Oak-C", oak, heatwave, 5, 6);

        ensurePlantInForest("Rain-Oak-A", oak, rain, 3, 2);
        ensurePlantInForest("Rain-Tomato-B", tomato, rain, 6, 6);
        ensurePlantInForest("Rain-Pine-C", pine, rain, 4, 8);

        ensurePlantInForest("Dry-Oak-A", oak, drought, 2, 2);
        ensurePlantInForest("Dry-Tomato-B", tomato, drought, 7, 3);
        ensurePlantInForest("Dry-Pine-C", pine, drought, 8, 7);

        ensurePlantInForest("Mix-Tomato-A", tomato, mixed, 1, 1);
        ensurePlantInForest("Mix-Tomato-B", tomato, mixed, 2, 1);
        ensurePlantInForest("Mix-Oak-C", oak, mixed, 6, 3);
        ensurePlantInForest("Mix-Pine-D", pine, mixed, 9, 4);
        ensurePlantInForest("Mix-Pine-E", pine, mixed, 10, 6);

        ensureStimulus("HEATWAVE", heatwave.getId(), 8.0, 12);
        ensureStimulus("RAIN", rain.getId(), 25.0, 12);
        ensureStimulus("HEATWAVE", drought.getId(), 9.5, 18);
        ensureStimulus("DROUGHT", drought.getId(), 30.0, 24);
        ensureStimulus("RAIN", mixed.getId(), 10.0, 6);
        ensureStimulus("HEATWAVE", mixed.getId(), 5.0, 6);
    }

    private void ensureSpecies(Species source) {
        Optional<Species> existing = speciesRepository.findByName(source.getName());
        if (existing.isPresent()) {
            return;
        }
        speciesRepository.save(source);
    }

    private void ensureEffect(Effect source) {
        Optional<Effect> existing = effectRepository.findByName(source.getName());
        if (existing.isPresent()) {
            return;
        }
        effectRepository.save(source);
    }

    private Forest ensureForest(String name, int width, int height, double lat, double lon) {
        return forestRepository.findByName(name)
                .map(forest -> {
                    if (forest.getCoords() == null || forest.getCoords().length < 2) {
                        forest.setCoords(new double[]{lat, lon});
                        return forestRepository.save(forest);
                    }
                    return forest;
                })
                .orElseGet(() -> {
                    Forest forest = new Forest(name, width, height);
                    forest.setCoords(new double[]{lat, lon});
                    return forestRepository.save(forest);
                });
    }

    private void ensurePlantInForest(String plantName, Species species, Forest forest, int x, int y) {
        Optional<Plant> existingPlant = plantRepository.findByNameContainingIgnoreCase(plantName).stream()
                .filter(p -> plantName.equalsIgnoreCase(p.getName()))
                .findFirst();

        Plant plant = existingPlant.orElseGet(() -> createPlant(plantName, species, forest.getId(), x, y));
        if (existingPlant.isPresent()) {
            if (plant.getForestId() == null || !forest.getId().equals(plant.getForestId())) {
                plant.setForestId(forest.getId());
            }
            if (plant.getX() == null || plant.getX() != x) {
                plant.setX(x);
            }
            if (plant.getY() == null || plant.getY() != y) {
                plant.setY(y);
            }
            plant.evaluateState();
            plant = plantRepository.save(plant);
        }

        addPlantToForest(forest, plant);
    }

    private void ensureStimulus(String type, String forestId, double intensity, int durationHours) {
        List<Stimulus> existing = stimulusRepository.findByForestId(forestId);
        boolean alreadyExists = existing.stream()
                .anyMatch(s -> type.equalsIgnoreCase(s.getType())
                        && s.getDurationHours() == durationHours
                        && Double.compare(s.getIntensity(), intensity) == 0);

        if (!alreadyExists) {
            stimulusRepository.save(new Stimulus(type, forestId, intensity, durationHours));
        }
    }

    private Plant createPlant(String name, Species species, String forestId, int x, int y) {
        Plant plant = new Plant(name, species);
        plant.setForestId(forestId);
        plant.setX(x);
        plant.setY(y);
        plant.evaluateState();
        return plantRepository.save(plant);
    }

    private void addPlantToForest(Forest forest, Plant plant) {
        if (forest.getPlants() == null) {
            forest.setPlants(new ArrayList<>());
        }

        boolean plantAlreadyReferenced = forest.getPlants().stream()
                .anyMatch(p -> p != null && p.getId() != null && p.getId().equals(plant.getId()));
        if (!plantAlreadyReferenced) {
            forest.getPlants().add(plant);
        }

        boolean cellAlreadyExists = forest.getCells().stream()
                .anyMatch(c -> c.getX() == plant.getX() && c.getY() == plant.getY());
        if (!cellAlreadyExists) {
            forest.getCells().add(new Forest.ForestCell(plant.getX(), plant.getY(), plant.getId()));
        }

        forestRepository.save(forest);
    }
}
