package org.example.entites.ecosystem;

import org.example.entites.ecosystem.diseases.PlantDisease;
import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;

import java.util.*;

public class EcosystemCell {
    private Forest.ForestCell forestCell;
    private PlantDisease disease; // null = pas malade

    public EcosystemCell(Forest.ForestCell forestCell) {
        this.forestCell = forestCell;
    }

    public boolean hasPlant() {
        return forestCell.getPlantId() != null;
    }

    public boolean isDiseased() {
        return disease != null;
    }

    public PlantDisease getDisease() {
        return disease;
    }

    public void infect(PlantDisease newDisease) {
        if (hasPlant() && disease == null) {
            this.disease = newDisease;
        }
    }

    public void recover() {
        this.disease = null;
    }

    public Forest.ForestCell getForestCell() {
        return forestCell;
    }

    public List<EcosystemCell> getNeighbors(org.example.entites.ecosystem.Ecosystem ecosystem) {
        List<EcosystemCell> neighbors = new ArrayList<>();
        int x = forestCell.getX();
        int y = forestCell.getY();

        for (EcosystemCell other : ecosystem.getCells()) {
            if (other == this){
                continue;
            }

            int otherX = other.getForestCell().getX();
            int otherY = other.getForestCell().getY();

            if (Math.abs(otherX - x) <= 1 && Math.abs(otherY - y) <= 1) {
                neighbors.add(other);
            }
        }

        return neighbors;
    }

    public void progressDisease(Plant plant) {
        if (disease != null) {
            disease.progress(plant);
        }
    }

    public PlantDisease getMostSevereNeighborDisease(org.example.entites.ecosystem.Ecosystem ecosystem) {
        List<EcosystemCell> neighbors = getNeighbors(ecosystem);

        // Map : nom de la maladie → (somme des sévérités, nombre d’occurrences)
        Map<String, double[]> severitySums = new HashMap<>();
        Map<String, PlantDisease> diseaseSamples = new HashMap<>();

        for (EcosystemCell neighbor : neighbors) {
            if (!neighbor.hasPlant() || !neighbor.isDiseased()) continue;

            PlantDisease d = neighbor.getDisease();
            String name = d.getName();

            // Stocker un exemple pour pouvoir copier le type plus tard
            diseaseSamples.putIfAbsent(name, d);

            // Mettre à jour somme et compte
            double[] sumCount = severitySums.getOrDefault(name, new double[]{0.0, 0.0});
            sumCount[0] += d.getSeverity(); // somme des sévérités
            sumCount[1] += 1;               // nombre d’occurrences
            severitySums.put(name, sumCount);
        }

        if (severitySums.isEmpty()) return null;

        // Trouver la maladie avec la sévérité moyenne la plus élevée
        String selectedName = null;
        double maxAvgSeverity = -1;

        for (Map.Entry<String, double[]> entry : severitySums.entrySet()) {
            double avgSeverity = entry.getValue()[0] / entry.getValue()[1];
            if (avgSeverity > maxAvgSeverity) {
                maxAvgSeverity = avgSeverity;
                selectedName = entry.getKey();
            }
        }

        // Retourner une nouvelle instance de la maladie majoritaire
        return selectedName != null ? diseaseSamples.get(selectedName).copy() : null;
    }

    public boolean shouldBecomeInfected(org.example.entites.ecosystem.Ecosystem ecosystem, double threshold) {
        if (!hasPlant()) {
            return false;
        }
        if (isDiseased()) {
            return false;
        }
        List<EcosystemCell> neighbors = getNeighbors(ecosystem);
        int infected = 0;
        int total = 0;
        for (EcosystemCell neighbor : neighbors) {
            if (!neighbor.hasPlant()){
                continue;
            }
            total++;
            if (neighbor.isDiseased()) {
                infected++;
            }
        }
        if (total == 0){
            return false;
        }
        double ratio = (double) infected / total;
        return ratio >= threshold;
    }

    public boolean shouldRecover(org.example.entites.ecosystem.Ecosystem ecosystem, double threshold) {

        if (!hasPlant()) {
            return false;
        }
        if (!isDiseased()) {
            return false;
        }
        List<EcosystemCell> neighbors = getNeighbors(ecosystem);
        int healthy = 0;
        int total = 0;
        for (EcosystemCell neighbor : neighbors) {
            if (!neighbor.hasPlant()) {
                continue;
            }
            total++;
            if (!neighbor.isDiseased()) {
                healthy++;
            }
        }
        if (total == 0){
            return false;
        }
        double ratio = (double) healthy / total;
        return ratio >= threshold;
    }
}
