package org.example.entities.placement;

/**
 * Represents constraints for plant placement optimization
 */
public class PlacementConstraint {

    // Species compatibility matrix: stores compatibility scores between species
    private String speciesA;
    private String speciesB;
    private double compatibilityScore; // -1.0 (incompatible) to 1.0 (highly beneficial)

    // Resource competition factors
    private double waterCompetitionRadius; // cells radius
    private double lightCompetitionRadius;

    // Disease resistance spacing
    private double minDiseaseSpacing; // minimum cells between same species

    public PlacementConstraint() {}

    public PlacementConstraint(String speciesA, String speciesB, double compatibilityScore) {
        this.speciesA = speciesA;
        this.speciesB = speciesB;
        this.compatibilityScore = compatibilityScore;

        // Default values
        this.waterCompetitionRadius = 2.0;
        this.lightCompetitionRadius = 2.5;
        this.minDiseaseSpacing = 3.0;
    }

    // Getters and Setters
    public String getSpeciesA() {
        return speciesA;
    }

    public void setSpeciesA(String speciesA) {
        this.speciesA = speciesA;
    }

    public String getSpeciesB() {
        return speciesB;
    }

    public void setSpeciesB(String speciesB) {
        this.speciesB = speciesB;
    }

    public double getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(double compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }

    public double getWaterCompetitionRadius() {
        return waterCompetitionRadius;
    }

    public void setWaterCompetitionRadius(double waterCompetitionRadius) {
        this.waterCompetitionRadius = waterCompetitionRadius;
    }

    public double getLightCompetitionRadius() {
        return lightCompetitionRadius;
    }

    public void setLightCompetitionRadius(double lightCompetitionRadius) {
        this.lightCompetitionRadius = lightCompetitionRadius;
    }

    public double getMinDiseaseSpacing() {
        return minDiseaseSpacing;
    }

    public void setMinDiseaseSpacing(double minDiseaseSpacing) {
        this.minDiseaseSpacing = minDiseaseSpacing;
    }
}
