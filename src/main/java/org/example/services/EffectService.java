package org.example.services;

import org.example.entites.effect.Effect;
import org.example.entites.plant.PlantEffect;
import org.example.repositories.EffectRepository;
import org.example.repositories.PlantEffectRepository;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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
                Effect.createHeatingEffect()
            );
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
        return effectRepository.findById(effectId);
    }
    
    /**
     * Applique un effet à une plante.
     */
    public PlantEffect applyEffectToPlant(String plantId, String effectId) throws Exception {
        // Vérifier que la plante existe
        if (!plantRepository.existsById(plantId)) {
            throw new Exception("Plante introuvable : " + plantId);
        }
        
        // Récupérer l'effet
        Effect effect = effectRepository.findById(effectId)
                .orElseThrow(() -> new Exception("Effet introuvable : " + effectId));
        
        // Créer l'association plante-effet
        PlantEffect plantEffect = new PlantEffect(
            plantId,
            effectId,
            LocalDateTime.now(),
            effect.getDurationHours()
        );
        
        return plantEffectRepository.save(plantEffect);
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
        PlantEffect plantEffect = plantEffectRepository.findById(plantEffectId)
                .orElseThrow(() -> new Exception("Association plante-effet introuvable : " + plantEffectId));
        
        plantEffect.setActive(false);
        plantEffectRepository.save(plantEffect);
    }
    
    /**
     * Calcule les modificateurs totaux appliqués par les effets actifs d'une plante.
     */
    public EffectModifiers calculateTotalModifiers(String plantId) {
        List<PlantEffect> activeEffects = getActivePlantEffects(plantId);
        EffectModifiers totalModifiers = new EffectModifiers();
        
        for (PlantEffect plantEffect : activeEffects) {
            Optional<Effect> effectOpt = effectRepository.findById(plantEffect.getEffectId());
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