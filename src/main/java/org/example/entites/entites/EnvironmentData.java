package org.example.entites;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Component
public class EnvironmentData {
    private LocalDateTime timestamp;
    private double temperature; // °C
    private double humidity;    // %
    private double lux;         // lumière
    private double rainfall;    // mm

    private List<Plant> plants = new ArrayList<>();

    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    public EnvironmentData(LocalDateTime timestamp, double temperature, double humidity, double lux, double rainfall) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.lux = lux;
        this.rainfall = rainfall;
    }

    public EnvironmentData(){
        this.timestamp = LocalDateTime.now();

        // Température : 15°C à 30°C
        this.temperature = 15 + Math.random() * 15;

        // Humidité : 30% à 80%
        this.humidity = 30 + Math.random() * 50;

        // Lumière : 0 lx (nuit) à 2000 lx (plein soleil)
        this.lux = Math.random() * 2000;

        // Pluie : 0 à 10 mm
        this.rainfall = Math.random() * 10;
    }

    public void evolve() {
        this.timestamp = this.timestamp.plusHours(1);

        // Cycle jour/nuit simple : lux dépend de l'heure
        int hour = this.timestamp.getHour();
        if(hour >= 6 && hour <= 18) {
            // Lumière du jour, pic à midi
            this.lux = 2000 * Math.sin(Math.PI * (hour - 6) / 12.0);
        } else {
            this.lux = 0; // nuit
        }

        // Température : plus chaud en journée, plus frais la nuit
        double tempVariation = Math.random() * 2 - 1; // ±1°C
        if(hour >= 6 && hour <= 18) {
            this.temperature = 20 + 10 * Math.sin(Math.PI * (hour - 6) / 12.0) + tempVariation;
        } else {
            this.temperature = 15 + tempVariation;
        }

        // Humidité : inversement proportionnelle à la température
        this.humidity = Math.max(0, Math.min(100, 60 - (this.temperature - 20) * 1.5 + (Math.random() * 5 - 2.5)));

        // Pluie : aléatoire, plus probable si humidité élevée
        this.rainfall = (Math.random() < this.humidity / 100) ? Math.random() * 5 : 0;
    }

    // getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getLux() { return lux; }
    public double getRainfall() { return rainfall; }
    public List<Plant> getPlants() { return plants; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setLux(double lux) { this.lux = lux; }
    public void setRainfall(double rainfall) { this.rainfall = rainfall; }
}
