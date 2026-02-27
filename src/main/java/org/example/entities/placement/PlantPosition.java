package org.example.entities.placement;

/**
 * Represents a plant's position and species in the forest grid
 */
public class PlantPosition {

    private String plantId;
    private String speciesId;
    private String speciesName;
    private int x;
    private int y;

    public PlantPosition() {}

    public PlantPosition(String plantId, String speciesId, String speciesName, int x, int y) {
        this.plantId = plantId;
        this.speciesId = speciesId;
        this.speciesName = speciesName;
        this.x = x;
        this.y = y;
    }

    // Copy constructor
    public PlantPosition(PlantPosition other) {
        this.plantId = other.plantId;
        this.speciesId = other.speciesId;
        this.speciesName = other.speciesName;
        this.x = other.x;
        this.y = other.y;
    }

    // Calculate Euclidean distance to another position
    public double distanceTo(PlantPosition other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Getters and Setters
    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("PlantPosition{species='%s', x=%d, y=%d}", speciesName, x, y);
    }
}
