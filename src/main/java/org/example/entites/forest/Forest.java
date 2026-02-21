package org.example.entites.forest;

import org.example.entites.plant.Plant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef; // <-- IMPORT AJOUTÉ
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "forests")
public class Forest {
    
    @Id
    private String id;
    
    @NotBlank(message = "Le nom de la forêt est obligatoire")
    private String name;
    
    @Min(value = 1, message = "La largeur doit être au moins 1")
    private int width;
    
    @Min(value = 1, message = "La hauteur doit être au moins 1")
    private int height;
    
    private LocalDateTime createdAt;
    
    // --- VOTRE ANCIEN CODE (ON LE GARDE) ---
    private List<ForestCell> cells;

    // --- NOUVEL AJOUT (POUR COMPATIBILITÉ) ---
    // On ajoute cette liste pour que DataInitializer et le Frontend s'y retrouvent
    @DBRef
    private List<Plant> plants = new ArrayList<>();
    
    // Constructeur
    public Forest(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.createdAt = LocalDateTime.now();
        this.cells = new ArrayList<>();
        this.plants = new ArrayList<>(); // Init nouvelle liste
    }
    
    // Constructeur par défaut pour MongoDB
    protected Forest() {
        this.cells = new ArrayList<>();
        this.plants = new ArrayList<>(); // Init nouvelle liste
    }
    
    // --- VOS MÉTHODES D'ORIGINE (ON NE TOUCHE PAS) ---
    
    public boolean isPositionOccupied(int x, int y) {
        // On vérifie d'abord l'ancien système
        boolean occupiedInCells = cells.stream().anyMatch(cell -> cell.getX() == x && cell.getY() == y);
        if (occupiedInCells) return true;

        // Sécurité : on regarde aussi dans la nouvelle liste (au cas où)
        return plants.stream().anyMatch(p -> p.getX() != null && p.getX() == x && p.getY() != null && p.getY() == y);
    }
    
    public ForestCell getCellAt(int x, int y) {
        return cells.stream()
                .filter(cell -> cell.getX() == x && cell.getY() == y)
                .findFirst()
                .orElse(null);
    }
    
    public void addCell(ForestCell cell) {
        if (cell.getX() < 0 || cell.getX() >= width || cell.getY() < 0 || cell.getY() >= height) {
            throw new IllegalArgumentException("Position hors des limites de la forêt");
        }
        if (isPositionOccupied(cell.getX(), cell.getY())) {
            throw new IllegalArgumentException("Position déjà occupée");
        }
        cells.add(cell);
    }
    
    public void removePlantAt(int x, int y) {
        // Supprime de l'ancienne liste
        cells.removeIf(cell -> cell.getX() == x && cell.getY() == y);
        // Supprime aussi de la nouvelle liste (pour garder la synchro)
        plants.removeIf(p -> p.getX() != null && p.getX() == x && p.getY() != null && p.getY() == y);
    }

    // --- NOUVELLES MÉTHODES (POUR RÉGLER L'ERREUR "UNDEFINED") ---

    // C'est cette méthode que DataInitializer cherchait !
    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    // Méthode hybride : Ajoute la plante dans la liste "plants" ET crée une "cell" pour la compatibilité
    public void addPlant(Plant plant) {
        // 1. Ajout dans la nouvelle liste
        this.plants.add(plant);

        // 2. Ajout dans l'ancienne liste (pour que votre vieux code marche encore)
        if (plant.getX() != null && plant.getY() != null) {
            // On vérifie qu'elle n'y est pas déjà pour éviter les doublons
            boolean alreadyInCells = cells.stream().anyMatch(c -> c.getX() == plant.getX() && c.getY() == plant.getY());
            if (!alreadyInCells) {
                cells.add(new ForestCell(plant.getX(), plant.getY(), plant.getId()));
            }
        }
    }
    
    // --- GETTERS ET SETTERS CLASSIQUES ---
    
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ForestCell> getCells() { return cells; }
    public void setCells(List<ForestCell> cells) { this.cells = cells; }
    
    // Classe interne (Votre code original)
    public static class ForestCell {
        private int x;
        private int y;
        private String plantId;
        
        public ForestCell(int x, int y, String plantId) {
            this.x = x;
            this.y = y;
            this.plantId = plantId;
        }
        
        public ForestCell() {}
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public String getPlantId() { return plantId; }
        public void setPlantId(String plantId) { this.plantId = plantId; }
    }
}