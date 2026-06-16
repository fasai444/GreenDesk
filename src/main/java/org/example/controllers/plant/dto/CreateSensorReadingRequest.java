package org.example.controllers.plant.dto;

import java.time.LocalDateTime;

public class CreateSensorReadingRequest {
    private LocalDateTime timestamp; // optional
    private double temperature;
    private double humidity;
    private double lux;
    private double rainfall;

    public CreateSensorReadingRequest() {}

    public LocalDateTime getTimestamp() { return timestamp; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }
    public double getLux() { return lux; }
    public double getRainfall() { return rainfall; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public void setLux(double lux) { this.lux = lux; }
    public void setRainfall(double rainfall) { this.rainfall = rainfall; }
}