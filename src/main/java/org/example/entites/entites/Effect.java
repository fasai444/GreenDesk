package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * Représente un effet prédéfini pouvant être appliqué à une plante.
 * Exemples : ombrage, fertilisant, arrosage supplémentaire, etc.
 */
@Document(collection = "effects")
public class Effect {
    
    @Id
    private String id;
    
    @NotBlank(message = "Le nom de l'effet est obligatoire")
    private String name;
    
    private String description;
    
    @Min(value = 1, message = "La durée doit être au moins 1 heure")
    private int durationHours;  // Durée de l'effet en heures
    
    // Modificateurs appliqués pendant la durée de l'effet
    private double temperatureModifier;
    private double humidityModifier;
    private double luxModifier;
    private double waterModifier;
    private double growthRateModifier;  // Modificateur du taux de croissance
    private double stressReduction;     // Réduction du stress
    
    public Effect(String name, String description, int durationHours) {
        this.name = name;
        this.description = description;
        this.durationHours = durationHours;
        this.temperatureModifier = 0.0;
        this.humidityModifier = 0.0;
        this.luxModifier = 0.0;
        this.waterModifier = 0.0;
        this.growthRateModifier = 0.0;
        this.stressReduction = 0.0;
    }
    
    // Constructeur par défaut pour MongoDB
    protected Effect() {}
    
    // Effets prédéfinis
    public static Effect createShadeEffect() {
        Effect effect = new Effect("Shade", "Réduit l'exposition à la lumière", 6);
        effect.setLuxModifier(-2000.0);  // Réduit la luminosité
        effect.setTemperatureModifier(-3.0);  // Réduit légèrement la température
        effect.setStressReduction(0.1);  // Réduit le stress lié à la lumière excessive
        return effect;
    }
    
    public static Effect createFertilizerEffect() {
        Effect effect = new Effect("Fertilizer", "Augmente la croissance", 12);
        effect.setGrowthRateModifier(0.5);  // Augmente la croissance de 50%
        effect.setWaterModifier(20.0);  // Améliore la rétention d'eau
        effect.setStressReduction(0.15);
        return effect;
    }
    
    public static Effect createWateringEffect() {
        Effect effect = new Effect("Extra Watering", "Arrosage supplémentaire", 4);
        effect.setWaterModifier(50.0);  // Augmente l'eau disponible
        effect.setHumidityModifier(10.0);  // Augmente l'humidité
        effect.setStressReduction(0.2);
        return effect;
    }
    
    public static Effect createHeatingEffect() {
        Effect effect = new Effect("Heating", "Augmente la température ambiante", 8);
        effect.setTemperatureModifier(5.0);  // Augmente la température
        effect.setStressReduction(0.1);
        return effect;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }
    
    public double getTemperatureModifier() {
        return temperatureModifier;
    }
    
    public void setTemperatureModifier(double temperatureModifier) {
        this.temperatureModifier = temperatureModifier;
    }
    
    public double getHumidityModifier() {
        return humidityModifier;
    }
    
    public void setHumidityModifier(double humidityModifier) {
        this.humidityModifier = humidityModifier;
    }
    
    public double getLuxModifier() {
        return luxModifier;
    }
    
    public void setLuxModifier(double luxModifier) {
        this.luxModifier = luxModifier;
    }
    
    public double getWaterModifier() {
        return waterModifier;
    }
    
    public void setWaterModifier(double waterModifier) {
        this.waterModifier = waterModifier;
    }
    
    public double getGrowthRateModifier() {
        return growthRateModifier;
    }
    
    public void setGrowthRateModifier(double growthRateModifier) {
        this.growthRateModifier = growthRateModifier;
    }
    
    public double getStressReduction() {
        return stressReduction;
    }
    
    public void setStressReduction(double stressReduction) {
        this.stressReduction = stressReduction;
    }
}
