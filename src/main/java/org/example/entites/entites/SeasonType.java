package org.example.entites;

public enum SeasonType {
    WINTER,
    SPRING,
    SUMMER,
    AUTUMN;
    
    // Méthode pour obtenir la saison suivante dans le cycle
    public SeasonType next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
