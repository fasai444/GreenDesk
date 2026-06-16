package org.example.services;

import org.example.entities.effect.Effect;
import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantEffect;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EffectService {

    @Autowired
    private EffectRepository effectRepository;

    @Autowired
    private PlantEffectRepository plantEffectRepository;

    @Autowired
    private PlantRepository plantRepository;

    /**
     * Initialise le catalogue d'effets prédéfinis s'il n'existe pas encore.
     */
    public void initializeEffectsCatalog() {
        if (effectRepository.count() == 0) {
            List<Effect> predefinedEffects = Arrays.asList(
                    Effect.createShadeEffect(),
                    Effect.createFertilizerEffect(),
                    Effect.createWateringEffect(),
                    Effect.createHeatingEffect());
            effectRepository.saveAll(predefinedEffects);
        }
    }

    /**
     * Récupère le catalogue de tous les effets.
     */
    public List<Effect> getAllEffects() {
        return effectRepository.findAll();
    }

    /**
     * Récupère le catalogue des effets, avec filtre optionnel custom/non-custom.
     */
    public List<Effect> getAllEffects(Boolean custom) {
        if (custom == null) {
            return effectRepository.findAll();
        }
        return effectRepository.findByIsCustom(custom);
    }

    /**
     * Crée un effet personnalisé (L3-F1).
     */
    public Effect createCustomEffect(Effect effect) {
        effect.setCustom(true); // On force le flag "Custom"
        return effectRepository.save(effect);
    }

    /**
     * Récupère un effet par son ID.
     */
    public Optional<Effect> getEffectById(String effectId) {
        return effectRepository.findById(Objects.requireNonNull(effectId, "effectId must not be null"));
    }

    /**
     * Applique un effet à une plante.
     */
    public PlantEffect applyEffectToPlant(String plantId, String effectId) throws Exception {
        String safePlantId = Objects.requireNonNull(plantId, "plantId must not be null");
        String safeEffectId = Objects.requireNonNull(effectId, "effectId must not be null");

        // Vérifier que la plante existe
        if (!plantRepository.existsById(safePlantId)) {
            throw new Exception("Plante introuvable : " + safePlantId);
        }

        // Récupérer l'effet
        Effect effect = effectRepository.findById(safeEffectId)
                .orElseThrow(() -> new Exception("Effet introuvable : " + safeEffectId));

        // Créer l'association plante-effet
        PlantEffect plantEffect = new PlantEffect(
                safePlantId,
                safeEffectId,
                LocalDateTime.now(),
                effect.getDurationHours());

        PlantEffect savedPlantEffect = plantEffectRepository.save(plantEffect);

        // Impact immédiat pour rendre l'effet visible sans attendre un cycle de
        // simulation
        plantRepository.findById(safePlantId).ifPresent(plant -> {
            applyImmediateEffectImpact(plant, effect);
            plantRepository.save(plant);
        });

        return savedPlantEffect;
    }

    private void applyImmediateEffectImpact(Plant plant, Effect effect) {
        if (effect.getTemperatureModifier() != 0.0) {
            plant.setTemperature(plant.getTemperature() + effect.getTemperatureModifier());
        }
        if (effect.getHumidityModifier() != 0.0) {
            double humidity = plant.getHumidity() + effect.getHumidityModifier();
            plant.setHumidity(Math.max(0.0, Math.min(100.0, humidity)));
        }
        if (effect.getLuxModifier() != 0.0) {
            double lux = plant.getLux() + effect.getLuxModifier();
            plant.setLux(Math.max(0.0, lux));
        }
        if (effect.getWaterModifier() != 0.0) {
            double water = plant.getWaterLevel() + effect.getWaterModifier();
            plant.setWaterLevel(Math.max(0.0, water));
        }

        if (effect.getStressReduction() != 0.0) {
            double adjustedStress = plant.getStressIndex() - effect.getStressReduction();
            plant.setStressIndex(Math.max(0.0, Math.min(1.0, adjustedStress)));
        }

        plant.setPlantState(plant.evaluateState());
    }

    /**
     * Récupère tous les effets d'une plante.
     */
    public List<PlantEffect> getPlantEffects(String plantId) {
        return plantEffectRepository.findByPlantId(plantId);
    }

    /**
     * Récupère les effets actifs d'une plante.
     */
    public List<PlantEffect> getActivePlantEffects(String plantId) {
        List<PlantEffect> effects = plantEffectRepository.findByPlantIdAndActive(plantId, true);

        // Mettre à jour le statut des effets (désactiver si expirés)
        LocalDateTime now = LocalDateTime.now();
        for (PlantEffect effect : effects) {
            effect.checkAndUpdateStatus(now);
            if (!effect.isActive()) {
                plantEffectRepository.save(effect);
            }
        }

        // Retourner seulement les effets encore actifs
        return effects.stream()
                .filter(PlantEffect::isActive)
                .toList();
    }

    /**
     * Retire (désactive) un effet d'une plante.
     */
    public void removeEffectFromPlant(String plantEffectId) throws Exception {
        String safePlantEffectId = Objects.requireNonNull(plantEffectId, "plantEffectId must not be null");
        PlantEffect plantEffect = plantEffectRepository.findById(safePlantEffectId)
                .orElseThrow(() -> new Exception("Association plante-effet introuvable : " + safePlantEffectId));

        plantEffect.setActive(false);
        plantEffectRepository.save(plantEffect);
    }

    /**
     * Calcule les modificateurs totaux appliqués par les effets actifs d'une
     * plante.
     */
    public EffectModifiers calculateTotalModifiers(String plantId) {
        List<PlantEffect> activeEffects = getActivePlantEffects(plantId);
        EffectModifiers totalModifiers = new EffectModifiers();

        for (PlantEffect plantEffect : activeEffects) {
            Optional<Effect> effectOpt = Optional.ofNullable(plantEffect.getEffectId())
                    .flatMap(effectRepository::findById);
            if (effectOpt.isPresent()) {
                Effect effect = effectOpt.get();
                totalModifiers.temperature += effect.getTemperatureModifier();
                totalModifiers.humidity += effect.getHumidityModifier();
                totalModifiers.lux += effect.getLuxModifier();
                totalModifiers.water += effect.getWaterModifier();
                totalModifiers.growthRate += effect.getGrowthRateModifier();
                totalModifiers.stressReduction += effect.getStressReduction();
            }
        }

        return totalModifiers;
    }

    /**
     * Classe pour représenter les modificateurs totaux.
     */
    public static class EffectModifiers {
        public double temperature = 0.0;
        public double humidity = 0.0;
        public double lux = 0.0;
        public double water = 0.0;
        public double growthRate = 0.0;
        public double stressReduction = 0.0;
    }
}