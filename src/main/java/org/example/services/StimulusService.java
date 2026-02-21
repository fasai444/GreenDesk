package org.example.services;

import org.example.entites.effect.Effect;
import org.example.entites.plant.Plant;
import org.example.entites.Stimulus;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.StimulusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StimulusService {

    @Autowired
    private StimulusRepository stimulusRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private EffectService effectService;

    @Autowired
    private EffectRepository effectRepository;

    /**
     * Applique un stimulus à toute une forêt (Exigence L3-F2).
     * Crée un effet système et l'attache à chaque plante de la forêt ciblée.
     */
    public Stimulus applyToForest(Stimulus stimulus) throws Exception {
        // Validation pour éviter le warning Null type safety
        Objects.requireNonNull(stimulus, "Le stimulus ne peut pas être nul");

        // 1. Enregistrer le stimulus dans l'historique
        Stimulus savedStimulus = stimulusRepository.save(stimulus);

        // 2. Récupérer toutes les plantes de la forêt concernée
        // Note: Assurez-vous que findByForestId est bien dans votre PlantRepository
        List<Plant> plants = plantRepository.findByForestId(stimulus.getForestId());

        if (plants.isEmpty()) {
            return savedStimulus; // Rien à appliquer si la forêt est vide
        }

        // 3. Créer l'objet Effect correspondant au Stimulus
        Effect stimEffect = new Effect(
            stimulus.getType(), 
            "Effet généré par stimulus: " + stimulus.getType(), 
            stimulus.getDurationHours()
        );

        // Configuration des modificateurs selon le type de stimulus
        if ("HEATWAVE".equalsIgnoreCase(stimulus.getType())) {
            stimEffect.setTemperatureModifier(stimulus.getIntensity());
            stimEffect.setStressReduction(-0.15); // Augmente le stress
        } else if ("RAIN".equalsIgnoreCase(stimulus.getType())) {
            stimEffect.setWaterModifier(stimulus.getIntensity());
            stimEffect.setHumidityModifier(10.0);
            stimEffect.setStressReduction(0.2); // Réduit le stress
        }

        stimEffect.setCustom(false); // Indique que c'est un effet système, pas utilisateur

        // 4. Sauvegarder l'effet en base pour générer un ID
        Effect savedEffect = effectRepository.save(stimEffect);

        // 5. Appliquer l'effet à chaque plante de la forêt
        for (Plant plant : plants) {
            effectService.applyEffectToPlant(plant.getId(), savedEffect.getId());
        }

        return savedStimulus;
    }

        /**
     * Clone une plante existante vers une autre forêt (Exigence L3-F2).
     */
   public Plant clonePlantToForest(String plantId, String targetForestId, int newX, int newY) throws Exception {
        
        // --- CORRECTION DU WARNING NULL SAFETY ---
        // On garantit que les IDs fournis ne sont pas nuls avant de les utiliser
        Objects.requireNonNull(plantId, "L'ID de la plante originale ne peut pas être nul");
        Objects.requireNonNull(targetForestId, "L'ID de la forêt cible ne peut pas être nul");

        // 1. Trouver la plante originale
        Plant original = plantRepository.findById(plantId)
                .orElseThrow(() -> new Exception("Plante originale introuvable avec l'ID: " + plantId));

        // 2. Créer la copie (Assure-toi que public Plant() {} est bien dans ton entité Plant)
        Plant clone = new Plant();
        
        // 3. Copie conforme des attributs selon ton fichier Plant.java
        clone.setName(original.getName() + " (Clone)");
        clone.setSpecies(original.getSpecies()); // Nécessite setSpecies() dans Plant.java
        clone.setWaterLevel(original.getWaterLevel());
        clone.setTemperature(original.getTemperature());
        clone.setHumidity(original.getHumidity());
        clone.setLux(original.getLux());
        clone.setStressIndex(original.getStressIndex());
        clone.setPlantState(original.getPlantState());
        clone.setHeightCm(original.getHeightCm());
        clone.setVariationSeed(original.getVariationSeed());
        
        // 4. Assigner la destination (Forêt B) et les nouvelles coordonnées
        clone.setForestId(targetForestId);
        clone.setX(newX);
        clone.setY(newY);

        // 5. Sauvegarder le clone en base
        return plantRepository.save(clone);
    }
}