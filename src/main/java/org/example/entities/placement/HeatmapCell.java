package org.example.entities.placement;

/**
 * Represents a cell in the optimization heatmap
 */
public class HeatmapCell {

    private int x;
    private int y;
    private double score; // 0.0 (worst) to 1.0 (best) for placing a specific species here
    private String recommendedSpecies; // Best species for this cell

    public HeatmapCell() {}

    public HeatmapCell(int x, int y, double score, String recommendedSpecies) {
        this.x = x;
        this.y = y;
        this.score = score;
        this.recommendedSpecies = recommendedSpecies;
    }

    // Getters and Setters
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getRecommendedSpecies() {
        return recommendedSpecies;
    }

    public void setRecommendedSpecies(String recommendedSpecies) {
        this.recommendedSpecies = recommendedSpecies;
    }
}
