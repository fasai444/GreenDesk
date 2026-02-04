package org.example.entites;

/**
 * Classe représentant une saison avec ses caractéristiques environnementales.
 * Les saisons sont prédéfinies et influencent l'environnement de la forêt.
 */
public class Season {
    
    private SeasonType type;
    private String name;
    private double temperatureModifier;  // Modificateur de température (ex: -5°C en hiver)
    private double humidityModifier;     // Modificateur d'humidité (ex: +10% au printemps)
    private double luxModifier;          // Modificateur de luminosité (ex: +500 lux en été)
    private double rainfallModifier;     // Modificateur de pluie
    
    public Season(SeasonType type, String name, double temperatureModifier, 
                  double humidityModifier, double luxModifier, double rainfallModifier) {
        this.type = type;
        this.name = name;
        this.temperatureModifier = temperatureModifier;
        this.humidityModifier = humidityModifier;
        this.luxModifier = luxModifier;
        this.rainfallModifier = rainfallModifier;
    }
    
    // Catalogue de saisons prédéfinies
    public static Season getWinter() {
        return new Season(
            SeasonType.WINTER,
            "Hiver",
            -8.0,   // Température plus basse
            -15.0,  // Humidité plus basse
            -2000.0, // Moins de lumière
            -30.0   // Moins de pluie
        );
    }
    
    public static Season getSpring() {
        return new Season(
            SeasonType.SPRING,
            "Printemps",
            5.0,    // Température douce
            10.0,   // Humidité moyenne
            1000.0, // Bonne lumière
            20.0    // Pluie modérée
        );
    }
    
    public static Season getSummer() {
        return new Season(
            SeasonType.SUMMER,
            "Été",
            12.0,   // Température élevée
            -10.0,  // Humidité plus basse (chaleur)
            3000.0, // Forte luminosité
            -20.0   // Moins de pluie
        );
    }
    
    public static Season getAutumn() {
        return new Season(
            SeasonType.AUTUMN,
            "Automne",
            0.0,    // Température moyenne
            5.0,    // Humidité moyenne
            -1000.0, // Luminosité réduite
            30.0    // Plus de pluie
        );
    }
    
    // Méthode pour obtenir une saison par type
    public static Season getByType(SeasonType type) {
        switch (type) {
            case WINTER: return getWinter();
            case SPRING: return getSpring();
            case SUMMER: return getSummer();
            case AUTUMN: return getAutumn();
            default: return getSpring();
        }
    }
    
    // Getters
    public SeasonType getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public double getTemperatureModifier() {
        return temperatureModifier;
    }
    
    public double getHumidityModifier() {
        return humidityModifier;
    }
    
    public double getLuxModifier() {
        return luxModifier;
    }
    
    public double getRainfallModifier() {
        return rainfallModifier;
    }
    
    // Setters
    public void setType(SeasonType type) {
        this.type = type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setTemperatureModifier(double temperatureModifier) {
        this.temperatureModifier = temperatureModifier;
    }
    
    public void setHumidityModifier(double humidityModifier) {
        this.humidityModifier = humidityModifier;
    }
    
    public void setLuxModifier(double luxModifier) {
        this.luxModifier = luxModifier;
    }
    
    public void setRainfallModifier(double rainfallModifier) {
        this.rainfallModifier = rainfallModifier;
    }
}
