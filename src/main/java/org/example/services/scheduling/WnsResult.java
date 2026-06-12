package org.example.services.scheduling;

import java.util.Map;

/**
 * Résultat du calcul WNS (Watering Need Score) pour une plante.
 */
public class WnsResult {

    public static final double THRESHOLD = 0.8;

    private final double score;
    private final Map<String, Object> breakdown;
    private final boolean rainWithin6Hours;
    private final boolean skipWatering;

    public WnsResult(double score, Map<String, Object> breakdown,
                     boolean rainWithin6Hours, boolean skipWatering) {
        this.score = score;
        this.breakdown = breakdown;
        this.rainWithin6Hours = rainWithin6Hours;
        this.skipWatering = skipWatering;
    }

    public double getScore() {
        return score;
    }

    public Map<String, Object> getBreakdown() {
        return breakdown;
    }

    public boolean isRainWithin6Hours() {
        return rainWithin6Hours;
    }

    public boolean isSkipWatering() {
        return skipWatering;
    }

    public boolean requiresTask() {
        return score > THRESHOLD && !skipWatering;
    }
}
