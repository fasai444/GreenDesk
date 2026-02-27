package org.example.entities.placement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete placement solution (chromosome in genetic algorithm)
 */
public class PlacementSolution implements Comparable<PlacementSolution> {

    private List<PlantPosition> plantPositions;
    private double fitnessScore;
    private int forestWidth;
    private int forestHeight;

    public PlacementSolution(int forestWidth, int forestHeight) {
        this.forestWidth = forestWidth;
        this.forestHeight = forestHeight;
        this.plantPositions = new ArrayList<>();
        this.fitnessScore = 0.0;
    }

    public PlacementSolution(PlacementSolution other) {
        this.forestWidth = other.forestWidth;
        this.forestHeight = other.forestHeight;
        this.plantPositions = new ArrayList<>();
        for (PlantPosition pos : other.plantPositions) {
            this.plantPositions.add(new PlantPosition(pos));
        }
        this.fitnessScore = other.fitnessScore;
    }

    public void addPlantPosition(PlantPosition position) {
        this.plantPositions.add(position);
    }

    public boolean isPositionOccupied(int x, int y) {
        return plantPositions.stream()
                .anyMatch(p -> p.getX() == x && p.getY() == y);
    }

    @Override
    public int compareTo(PlacementSolution other) {
        return Double.compare(other.fitnessScore, this.fitnessScore); // Higher fitness first
    }

    // Getters and Setters
    public List<PlantPosition> getPlantPositions() {
        return plantPositions;
    }

    public void setPlantPositions(List<PlantPosition> plantPositions) {
        this.plantPositions = plantPositions;
    }

    public double getFitnessScore() {
        return fitnessScore;
    }

    public void setFitnessScore(double fitnessScore) {
        this.fitnessScore = fitnessScore;
    }

    public int getForestWidth() {
        return forestWidth;
    }

    public void setForestWidth(int forestWidth) {
        this.forestWidth = forestWidth;
    }

    public int getForestHeight() {
        return forestHeight;
    }

    public void setForestHeight(int forestHeight) {
        this.forestHeight = forestHeight;
    }
}
