package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;


@Document(collection ="plants")
public class Plant {
    @Id
    private String id;

    @NotBlank
    private String name;

    @DBRef
    private Species species;

    //Attributs de la plante, ne sont pas final car ils vont varier et évoluer
    private double waterLevel; // mL
    private double temperature; // °C
    private double humidity; // %
    private double lux; // lx

    private double stressIndex;
    private PlantState plantState; //état de la plante en fonction de son évaluation par rapport aux conditions optimales de l'espèce

    private double heightCm;      // hauteur en cm
    
    // Nouveaux attributs pour L2-F1 : gestion des forêts
    private String forestId;      // ID de la forêt (optionnel)
    private Integer x;            // Position X dans la forêt (optionnel)
    private Integer y;            // Position Y dans la forêt (optionnel)
    private int variationSeed;    // Graine de variation pour R2 (diversité)
    //--------------CONSTRCUTEURS--------------
    //Constructeur à utiliser lorsque l'on créè une plante avec des données de l'environnement
    public Plant(String name, Species species, double waterLevel, double temperature, double humidity, double lux) {
        this.name = name;
        this.species = species;
        this.waterLevel = waterLevel;
        this.temperature = temperature;
        this.humidity = humidity;
        this.lux = lux;

        // Calcul du stress initial en fonction des conditions
        this.stressIndex = calculateStressIndex();
        this.plantState = evaluateState();
        this.heightCm = 0.0;
        
        // Initialiser variationSeed avec une valeur aléatoire
        this.variationSeed = (int) (Math.random() * 1000000);
    }

    //Constructeur à utiliser pour des test
    public Plant(String name, Species species) {
        this.name = name;
        this.species = species;

        // Génère des valeurs aléatoires autour de l'optimal ± tolérance de l'espèce donné
        this.waterLevel = species.getOptimalWaterNeeds() + (Math.random() * 30 - 15);   // ±15 mL
        this.temperature = species.getOptimalTemperature() + (Math.random() * 8 - 4);  // ±4°C
        this.humidity = species.getOptimalHumidity() + (Math.random() * 20 - 10);      // ±10%
        this.lux = species.getOptimalLuxNeeds() + (Math.random() * 200 - 100);         // ±100 lx
        this.stressIndex = 0.0;
        this.plantState = PlantState.HEALTHY; //car on est dans les plages optimales
        this.heightCm = 0.0;
        
        // Initialiser variationSeed avec une valeur aléatoire
        this.variationSeed = (int) (Math.random() * 1000000);
    }
    protected Plant() {}


    // Méthode pour calculer le stress initial
    private double calculateStressIndex() {
        double stress = 0.0;

        // Stress lié à l'eau : plus l'écart est grand, plus le stress est élevé
        double waterDiff = Math.abs(this.waterLevel - species.getOptimalWaterNeeds());
        stress += Math.min(1.0, waterDiff / species.getOptimalWaterNeeds()); // normalisé entre 0 et 1

        // Stress lié à la température
        double tempDiff = 0.0;
        if(this.temperature < species.getOptimalTemperature()) {
            tempDiff = species.getOptimalTemperature() - this.temperature;
        } else if(this.temperature > species.getOptimalTemperature()) {
            tempDiff = this.temperature - species.getOptimalTemperature();
        }
        stress += Math.min(1.0, tempDiff / 10.0); // écart max 10°C normalisé

        // Stress lié à l'humidité
        double humidityDiff = 0.0;
        if(this.humidity < species.getOptimalHumidity()) {
            humidityDiff = species.getOptimalHumidity() - this.humidity;
        } else if(this.humidity > species.getOptimalHumidity()) {
            humidityDiff = this.humidity - species.getOptimalHumidity();
        }
        stress += Math.min(1.0, humidityDiff / 20.0); // écart max 20% normalisé

        // Stress lié à la lumière
        double lightStress = species.lightStressFactor(this.lux); // déjà normalisé entre 0 et 1
        stress += Math.min(1.0, lightStress);

        return Math.min(1.0, stress); // stress max = 1
    }


    public PlantState evaluateState() {
        // Calcule le stress global
        this.stressIndex = calculateStressIndex();

        // Détermine l'état en fonction du stress
        if (stressIndex < 0.3) {
            return PlantState.HEALTHY;
        } else if (stressIndex < 0.6) {
            return PlantState.STRESSED;
        } else if (stressIndex < 0.9) {
            return PlantState.DORMANT;
        } else {
            return PlantState.DISEASED;
        }
    }



    //--------------GETTERS ET SETTERS--------------
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Species getSpecies() {
        return species;
    }
    public double getWaterLevel() {
        return waterLevel;
    }
    public double getTemperature() {
        return temperature;
    }
    public double getHumidity() {
        return humidity;
    }
    public double getLux() {
        return lux;
    }
    public double getStressIndex() {
        return stressIndex;
    }
    public PlantState getPlantState() {
        return plantState;
    }
    public double getHeightCm() {
        return heightCm;
    }

    public void setWaterLevel(double waterLevel) {
        this.waterLevel = waterLevel;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
    public void setLux(double lux) {
        this.lux = lux;
    }
    public void setStressIndex(double stressIndex) {
        this.stressIndex = stressIndex;
    }
    public void setPlantState(PlantState plantState) {
        this.plantState = plantState;
    }
    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }
    
    public String getForestId() {
        return forestId;
    }
    
    public void setForestId(String forestId) {
        this.forestId = forestId;
    }
    
    public Integer getX() {
        return x;
    }
    
    public void setX(Integer x) {
        this.x = x;
    }
    
    public Integer getY() {
        return y;
    }
    
    public void setY(Integer y) {
        this.y = y;
    }
    
    public int getVariationSeed() {
        return variationSeed;
    }
    
    public void setVariationSeed(int variationSeed) {
        this.variationSeed = variationSeed;
    }
}
