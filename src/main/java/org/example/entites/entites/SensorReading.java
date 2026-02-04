package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entité représentant une lecture capteur (historique des conditions environnementales par plante)
 */
@Document(collection = "sensor_readings")
public class SensorReading {

    @Id
    private String id;

    private String plantId;           // Référence à l'ID de la plante (Plant.id)
    private LocalDateTime timestamp;
    private double temperature;       // °C
    private double humidity;          // %
    private double lux;               // lux
    private double rainfall;          // mm

    // -------------- CONSTRUCTEURS --------------

    public SensorReading() {
        // Constructeur vide requis par Spring Data / MongoDB
    }

    public SensorReading(String plantId, LocalDateTime timestamp,
                         double temperature, double humidity,
                         double lux, double rainfall) {
        this.plantId = plantId;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.lux = lux;
        this.rainfall = rainfall;
    }

    // -------------- GETTERS ET SETTERS --------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getLux() {
        return lux;
    }

    public void setLux(double lux) {
        this.lux = lux;
    }

    public double getRainfall() {
        return rainfall;
    }

    public void setRainfall(double rainfall) {
        this.rainfall = rainfall;
    }

    // -------------- OPTIONNEL : toString() pour debug --------------

    @Override
    public String toString() {
        return "SensorReading{" +
                "id='" + id + '\'' +
                ", plantId='" + plantId + '\'' +
                ", timestamp=" + timestamp +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", lux=" + lux +
                ", rainfall=" + rainfall +
                '}';
    }
}