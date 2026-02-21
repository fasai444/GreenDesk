package org.example;

import org.example.entites.plant.Plant;
import org.example.entites.plant.PlantState;
import org.example.entites.species.Species;

public class Test {
    public static void main(String[] args) {

        // ----------------- TEST SPECIES -----------------
        Species tomato = new Species("Tomato"); // valeurs aléatoires
        System.out.println("=== Species Test ===");
        System.out.println("Name: " + tomato.getName());
        System.out.println("Optimal Water (mL): " + tomato.getOptimalWaterNeeds());
        System.out.println("Optimal Temp (°C): " + tomato.getOptimalTemperature());
        System.out.println("Optimal Humidity (%): " + tomato.getOptimalHumidity());
        System.out.println("Optimal Lux (lx): " + tomato.getOptimalLuxNeeds());
        System.out.println("Seed Production Rate: " + tomato.getSeedProductionRate());

        // Vérifier les méthodes isOptimalX()
        double testWater = tomato.getOptimalWaterNeeds() + 10;
        System.out.println("Water " + testWater + " is optimal? " + tomato.isOptimalWaterNeeds(testWater));
        double testTemp = tomato.getOptimalTemperature() - 3;
        System.out.println("Temperature " + testTemp + " is optimal? " + tomato.isOptimalTemperature(testTemp));
        double testHumidity = tomato.getOptimalHumidity() + 5;
        System.out.println("Humidity " + testHumidity + " is optimal? " + tomato.isOptimalHumidity(testHumidity));
        double testLux = tomato.getOptimalLuxNeeds() - 500;
        System.out.println("Light stress factor: " + tomato.lightStressFactor(testLux));

        // ----------------- TEST PLANT -----------------
        Plant plant1 = new Plant("MyTomato", tomato); // constructeur test aléatoire
        System.out.println("\n=== Plant Test ===");
        System.out.println("Plant name: " + plant1.getName());
        System.out.println("Water: " + plant1.getWaterLevel());
        System.out.println("Temperature: " + plant1.getTemperature());
        System.out.println("Humidity: " + plant1.getHumidity());
        System.out.println("Lux: " + plant1.getLux());
        System.out.println("Initial State: " + plant1.getPlantState());

        // Évaluer son état
        PlantState state = plant1.evaluateState();
        System.out.println("Evaluated State: " + state);

        // Modifier un attribut pour tester le stress
        plant1.setWaterLevel(plant1.getWaterLevel() + 50); // dépasser tolérance
        PlantState newState = plant1.evaluateState();
        System.out.println("After changing water level, State: " + newState);
    }
}