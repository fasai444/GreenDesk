package org.example.entites;

public class Intervention {
    public enum InterventionType {
        WATER,
        PRUNE,
        SHADING
    }

    private InterventionType type;
    private double value; // quantité d'eau, taille à couper, réduction de lux...

    public Intervention(InterventionType type, double value) {
        this.type = type;
        this.value = value;
    }

    public InterventionType getType() { return type; }
    public double getValue() { return value; }
}
