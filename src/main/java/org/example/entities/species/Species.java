package org.example.entities.species;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.NotBlank;

@Document(collection ="species")
public class Species {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "on doit avoir le nom de l'espece")
    private String name;

    private double optimalWaterNeeds;
    private double optimalTemperature;
    private double optimalHumidity;
    private double optimalLuxNeeds;
    private double baseGrowthRate; // cm par cycle
    private double seedProductionRate;

    private boolean sensitiveToFrost = true;
    private boolean sensitiveToHeat = true;
    private boolean sensitiveToHeavyRain = true;
    private boolean sensitiveToWind = true;
    private boolean sensitiveToUV = true;

    //--------------CONSTRUCTEURS--------------

    // Constructeur vide (Modifié en Public pour que Spring/DataInitializer puisse l'utiliser librement)
    public Species() {}

    // Constructeur complet
    public Species(String name, double optimalWaterNeeds, double optimalTemperature, double optimalHumidity, double optimalLuxNeeds, double baseGrowthRate, double seedProductionRate) {
        this.name = name;
        this.optimalWaterNeeds = optimalWaterNeeds;
        this.optimalTemperature = optimalTemperature;
        this.optimalHumidity = optimalHumidity;
        this.optimalLuxNeeds = optimalLuxNeeds;
        this.baseGrowthRate = baseGrowthRate;
        this.seedProductionRate = seedProductionRate;
    }

    // Constructeur aléatoire pour tests
    public Species(String name){
        this.name = name;
        this.optimalWaterNeeds = 100 + Math.random()*400;
        this.optimalTemperature = 15 + Math.random()*15;
        this.optimalHumidity = 30 + Math.random() * 50;
        this.optimalLuxNeeds = 1000 + Math.random() * 9000;
        this.baseGrowthRate = 0.1 + Math.random() * 0.9;
        this.seedProductionRate = 0.1 + Math.random()*0.9;
    }

    //--------------LOGIQUE METIER (VOTRE CODE)--------------

    public boolean isOptimalWaterNeeds(double waterLevel) {
        return Math.abs(optimalWaterNeeds - waterLevel) <= 15;
    }

    public boolean isOptimalTemperature(double temperature) {
        return Math.abs(optimalTemperature - temperature) <= 4;
    }

    public boolean isOptimalHumidity(double humidity) {
        return Math.abs(optimalHumidity - humidity) <= 10;
    }

    public double tempStressFactor(double temperature) {
        double diff = Math.abs(temperature - optimalTemperature);
        double tolerance = 4;
        if (diff <= tolerance) return 0.0;
        return Math.min(1.0, (diff - tolerance) / optimalTemperature);
    }

    public double humidityStressFactor(double humidity) {
        double diff = Math.abs(humidity - optimalHumidity);
        double tolerance = 10;
        if (diff <= tolerance) return 0.0;
        return Math.min(1.0, (diff - tolerance) / optimalHumidity);
    }

    public double lightStressFactor(double lux){
        return Math.max(0, (optimalLuxNeeds - lux)/optimalLuxNeeds);
    }

    //--------------GETTERS (EXISTANTS)--------------
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getOptimalWaterNeeds() { return optimalWaterNeeds; }
    public double getOptimalTemperature() { return optimalTemperature; }
    public double getOptimalHumidity() { return optimalHumidity; }
    public double getOptimalLuxNeeds() { return optimalLuxNeeds; }
    public double getSeedProductionRate() { return seedProductionRate; }
    public double getBaseGrowthRate() { return baseGrowthRate; }

    //--------------SETTERS (AJOUTÉS POUR CORRIGER L'ERREUR)--------------
    
    // Ces méthodes manquaient et sont nécessaires pour le DataInitializer
    
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOptimalWaterNeeds(double optimalWaterNeeds) {
        this.optimalWaterNeeds = optimalWaterNeeds;
    }

    public void setOptimalTemperature(double optimalTemperature) {
        this.optimalTemperature = optimalTemperature;
    }

    public void setOptimalHumidity(double optimalHumidity) {
        this.optimalHumidity = optimalHumidity;
    }

    public void setOptimalLuxNeeds(double optimalLuxNeeds) {
        this.optimalLuxNeeds = optimalLuxNeeds;
    }

    public void setBaseGrowthRate(double baseGrowthRate) {
        this.baseGrowthRate = baseGrowthRate;
    }

    public void setSeedProductionRate(double seedProductionRate) {
        this.seedProductionRate = seedProductionRate;
    }

    // Ajout utile pour le débogage (System.out.println)
    @Override
    public String toString() {
        return "Species{name='" + name + "', temp=" + optimalTemperature + "}";
    }

    public boolean isSensitiveToFrost() {
    return sensitiveToFrost;
}

    public void setSensitiveToFrost(boolean sensitiveToFrost) {
        this.sensitiveToFrost = sensitiveToFrost;
    }

    public boolean isSensitiveToHeat() {
        return sensitiveToHeat;
    }

    public void setSensitiveToHeat(boolean sensitiveToHeat) {
        this.sensitiveToHeat = sensitiveToHeat;
    }

    public boolean isSensitiveToHeavyRain() {
        return sensitiveToHeavyRain;
    }

    public void setSensitiveToHeavyRain(boolean sensitiveToHeavyRain) {
        this.sensitiveToHeavyRain = sensitiveToHeavyRain;
    }

    public boolean isSensitiveToWind() {
        return sensitiveToWind;
    }

    public void setSensitiveToWind(boolean sensitiveToWind) {
        this.sensitiveToWind = sensitiveToWind;
    }

    public boolean isSensitiveToUV() {
        return sensitiveToUV;
    }

    public void setSensitiveToUV(boolean sensitiveToUV) {
        this.sensitiveToUV = sensitiveToUV;
    }
}