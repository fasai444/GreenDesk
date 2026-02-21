package org.example.services;

import org.example.entites.forest.Forest;
import org.example.entites.forest.Forest.ForestCell;
import org.example.entites.plant.Plant;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ForestService {
    
    @Autowired
    private ForestRepository forestRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    /**
     * Crée une nouvelle forêt.
     */
    public Forest createForest(String name, int width, int height) {
        Forest forest = new Forest(name, width, height);
        return forestRepository.save(forest);
    }
    
    /**
     * Récupère toutes les forêts.
     */
    public List<Forest> getAllForests() {
        return forestRepository.findAll();
    }
    
    /**
     * Récupère une forêt par son ID.
     */
    public Optional<Forest> getForestById(String forestId) {
        return forestRepository.findById(forestId);
    }
    
    /**
     * Ajoute une plante à une forêt à une position (x, y).
     * 
     * R1 : Vérifie l'unicité de position (x,y) dans la forêt.
     * R2 : Vérifie la diversité (pas de clones exacts).
     * 
     * @throws IllegalArgumentException si position occupée ou plante clone
     * @throws Exception si forêt ou plante introuvable
     */
    public Forest addPlantToForest(String forestId, String plantId, int x, int y) throws Exception {
        // Récupérer la forêt
        Forest forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new Exception("Forêt introuvable : " + forestId));
        
        // Récupérer la plante
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new Exception("Plante introuvable : " + plantId));
        
        // R1 : Vérifier l'unicité de position
        if (forest.isPositionOccupied(x, y)) {
            throw new IllegalArgumentException(
                "Position (" + x + "," + y + ") déjà occupée dans la forêt " + forestId
            );
        }
        
        // R2 : Vérifier la diversité (pas de clones exacts dans la même forêt)
        checkPlantDiversity(forest, plant);
        
        // Vérifier que la position est dans les limites
        if (x < 0 || x >= forest.getWidth() || y < 0 || y >= forest.getHeight()) {
            throw new IllegalArgumentException(
                "Position (" + x + "," + y + ") hors des limites de la forêt"
            );
        }
        
        // Ajouter la cellule à la forêt
        ForestCell cell = new ForestCell(x, y, plantId);
        forest.addCell(cell);
        
        // Mettre à jour les informations de position dans la plante
        plant.setForestId(forestId);
        plant.setX(x);
        plant.setY(y);
        
        // Sauvegarder les modifications
        plantRepository.save(plant);
        return forestRepository.save(forest);
    }
    
    /**
     * R2 : Vérifie qu'il n'existe pas déjà une plante identique dans la forêt.
     * Deux plantes sont considérées comme des clones si :
     * - Même espèce (ID)
     * - Même variationSeed
     * - Caractéristiques trop similaires
     */
    private void checkPlantDiversity(Forest forest, Plant newPlant) throws IllegalArgumentException {
        // Récupérer toutes les plantes de la forêt
        List<Plant> plantsInForest = getPlantsInForest(forest.getId());
        
        for (Plant existingPlant : plantsInForest) {
            // Vérifier si c'est la même espèce
            if (existingPlant.getSpecies().getId().equals(newPlant.getSpecies().getId())) {
                // Si même espèce et même variationSeed : clone interdit
                if (existingPlant.getVariationSeed() == newPlant.getVariationSeed()) {
                    throw new IllegalArgumentException(
                        "Une plante identique (même espèce et même variationSeed) existe déjà dans cette forêt. " +
                        "Les plantes doivent être diversifiées (R2)."
                    );
                }
                
                // Vérification supplémentaire : caractéristiques trop similaires
                if (arePlantsTooCimilar(existingPlant, newPlant)) {
                    throw new IllegalArgumentException(
                        "Une plante avec des caractéristiques trop similaires existe déjà dans cette forêt. " +
                        "Les plantes doivent être diversifiées (R2)."
                    );
                }
            }
        }
    }
    
    /**
     * Vérifie si deux plantes ont des caractéristiques trop similaires.
     */
    private boolean arePlantsTooCimilar(Plant plant1, Plant plant2) {
        double threshold = 0.01; // Seuil de similarité (1%)
        
        boolean waterSimilar = Math.abs(plant1.getWaterLevel() - plant2.getWaterLevel()) < threshold * plant1.getWaterLevel();
        boolean tempSimilar = Math.abs(plant1.getTemperature() - plant2.getTemperature()) < threshold * 100;
        boolean humiditySimilar = Math.abs(plant1.getHumidity() - plant2.getHumidity()) < threshold * 100;
        boolean luxSimilar = Math.abs(plant1.getLux() - plant2.getLux()) < threshold * plant1.getLux();
        
        // Si toutes les caractéristiques sont similaires, considérer comme clone
        return waterSimilar && tempSimilar && humiditySimilar && luxSimilar;
    }
    
    /**
     * Récupère toutes les plantes d'une forêt.
     */
    public List<Plant> getPlantsInForest(String forestId) {
        Forest forest = forestRepository.findById(forestId).orElse(null);
        if (forest == null) {
            return List.of();
        }
        
        // Récupérer les IDs des plantes depuis les cellules
        List<String> plantIds = forest.getCells().stream()
                .map(ForestCell::getPlantId)
                .collect(Collectors.toList());
        
        // Récupérer les plantes
        return plantRepository.findAllById(plantIds);
    }
    
    /**
     * Retire une plante d'une forêt.
     */
    public Forest removePlantFromForest(String forestId, int x, int y) throws Exception {
    Forest forest = forestRepository.findById(forestId)
            .orElseThrow(() -> new Exception("Forêt introuvable : " + forestId));
    
    // CORRECTION : On déballe l'Optional avec .orElse(null)
    ForestCell cell = forest.getCellAt(x, y).orElse(null); 
    
    if (cell == null) {
        throw new Exception("Aucune plante à la position (" + x + "," + y + ")");
    }
    
    // Récupérer la plante et nettoyer ses informations
    Optional<Plant> plantOpt = plantRepository.findById(cell.getPlantId());
    if (plantOpt.isPresent()) {
        Plant plant = plantOpt.get();
        plant.setForestId(null);
        plant.setX(null);
        plant.setY(null);
        plantRepository.save(plant);
    }
    
    // CORRECTION : Si removePlantAt n'existe pas, on filtre la liste des cellules directement
    forest.getCells().removeIf(c -> c.getX() == x && c.getY() == y);
    
    return forestRepository.save(forest);
}
    
    /**
     * Supprime une forêt.
     */
    public void deleteForest(String forestId) throws Exception {
        Forest forest = forestRepository.findById(forestId)
                .orElseThrow(() -> new Exception("Forêt introuvable : " + forestId));
        
        // Nettoyer les références dans les plantes
        List<Plant> plantsInForest = getPlantsInForest(forestId);
        for (Plant plant : plantsInForest) {
            plant.setForestId(null);
            plant.setX(null);
            plant.setY(null);
            plantRepository.save(plant);
        }
        
        forestRepository.delete(forest);
    }
}
