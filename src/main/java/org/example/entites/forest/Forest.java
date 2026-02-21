package org.example.entites;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    // ────────────────────────────────────────────────
    // Choix : on garde **UN SEUL** système de stockage
    // On utilise ici l'approche "embedded cells" (ForestCell)
    // On supprime la liste @DBRef plants pour éviter la duplication
    // ────────────────────────────────────────────────
// Liste pour compatibilité avec DataInitializer et ancien frontend
private List<Plant> plants = new ArrayList<>();


    private List<ForestCell> cells = new ArrayList<>();

    // Constructeurs
public Forest() {
    this.cells = new ArrayList<>();
    this.plants = new ArrayList<>();
    this.createdAt = LocalDateTime.now();
}

public Forest(String name, int width, int height) {
    this();
    this.name = name;
    this.width = width;
    this.height = height;
}
    

    // ────────────────────────────────────────────────
    // Méthodes de gestion des positions
    // ────────────────────────────────────────────────

    /**
     * Vérifie si une position est déjà occupée
     */
    
    public boolean isPositionOccupied(int x, int y) {
        return cells.stream()
                .anyMatch(cell -> cell.getX() == x && cell.getY() == y);
    }

    /**
     * Retourne la cellule à une position donnée (ou null)
     */
    public Optional<ForestCell> getCellAt(int x, int y) {
        return cells.stream()
                .filter(cell -> cell.getX() == x && cell.getY() == y)
                .findFirst();
    }

    /**
     * Ajoute une cellule (position + plante)
     * Lance une exception si la position est occupée ou hors limite
     */
    public void addCell(ForestCell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null");
        }

        int x = cell.getX();
        int y = cell.getY();

        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException(
                String.format("Position (%d, %d) hors limites (%dx%d)", x, y, width, height)
            );
        }

        if (isPositionOccupied(x, y)) {
            throw new IllegalArgumentException(
                String.format("Position (%d, %d) déjà occupée", x, y)
            );
        }

        cells.add(cell);
    }

    /**
     * Supprime la cellule correspondant à une plante donnée
     */
    public boolean removeCellByPlantId(String plantId) {
        if (plantId == null) return false;
        return cells.removeIf(cell -> plantId.equals(cell.getPlantId()));
    }

    /**
     * Supprime la cellule à une position donnée
     */
    public boolean removeCellAt(int x, int y) {
        return cells.removeIf(cell -> cell.getX() == x && cell.getY() == y);
    }

    // ────────────────────────────────────────────────
    // Getters / Setters
    // ────────────────────────────────────────────────
public List<Plant> getPlants() {
    return plants;
}

public void setPlants(List<Plant> plants) {
    this.plants = (plants != null) ? plants : new ArrayList<>();
}

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ForestCell> getCells() {
        return cells;
    }

    public void setCells(List<ForestCell> cells) {
        this.cells = (cells != null) ? cells : new ArrayList<>();
    }

    // ────────────────────────────────────────────────
    // Classe interne ForestCell (inchangée)
    // ────────────────────────────────────────────────

    public static class ForestCell {
        private int x;
        private int y;
        private String plantId;

        public ForestCell() {
        }

        public ForestCell(int x, int y, String plantId) {
            this.x = x;
            this.y = y;
            this.plantId = plantId;
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

        public String getPlantId() {
            return plantId;
        }

        public void setPlantId(String plantId) {
            this.plantId = plantId;
        }

        

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ForestCell that = (ForestCell) o;
            return x == that.x && y == that.y && Objects.equals(plantId, that.plantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, plantId);
        }
    }
}