package org.example.entites.ecosystem;

import org.example.entites.forest.Forest;

import java.util.ArrayList;
import java.util.List;

public class Ecosystem {

    private final List<EcosystemCell> cells = new ArrayList<>();
    private Forest forest;

    private final double infectionThreshold = 0.5;
    private final double recoveryThreshold = 0.6;

    public Ecosystem(Forest forest) {
        this.forest = forest;
    }

    public void initialiseEcosystem() {
        List<Forest.ForestCell> forestCells = forest.getCells();
        for(Forest.ForestCell cell : forestCells){
            EcosystemCell ecosystemCell = new EcosystemCell(cell);
            cells.add(ecosystemCell);
        }
    }

    public List<EcosystemCell> getCells() {
        return cells;
    }

    public Forest getForest() {
        return forest;
    }

    public String getForestId() {
        return forest != null ? forest.getId() : null;
    }

}