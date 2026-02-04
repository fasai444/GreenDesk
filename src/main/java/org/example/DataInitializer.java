package org.example;


import org.example.entites.Forest;
import org.example.entites.Plant;
import org.example.entites.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SpeciesRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SpeciesRepository speciesRepository;
    private final PlantRepository plantRepository;
    private final ForestRepository forestRepository;

    // Injection des dépendances (Les outils pour parler à la BDD)
    public DataInitializer(SpeciesRepository speciesRepository,
                           PlantRepository plantRepository,
                           ForestRepository forestRepository) {
        this.speciesRepository = speciesRepository;
        this.plantRepository = plantRepository;
        this.forestRepository = forestRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // On vérifie si la base est vide pour ne pas créer des doublons à chaque redémarrage
        if (forestRepository.count() == 0) {
            System.out.println("🌱 Démarrage de l'initialisation des données...");

            // ----------------- 1. CRÉATION DE L'ESPÈCE (Venant de votre Test.java) -----------------
            Species tomato = new Species("Tomato");
            // On s'assure que les valeurs sont bien définies (si le constructeur est aléatoire, c'est bon)
            // Si besoin, on force des valeurs pour l'exemple :
            if (tomato.getOptimalWaterNeeds() == 0) tomato.setOptimalWaterNeeds(200.0);
            if (tomato.getOptimalTemperature() == 0) tomato.setOptimalTemperature(22.0);
            
            // SAUVEGARDE EN BDD
            speciesRepository.save(tomato);
            System.out.println("✅ Espèce sauvegardée : " + tomato.getName());

            // ----------------- 2. CRÉATION DE LA FORÊT (Indispensable pour le Frontend) -----------------
            Forest forest = new Forest("Jardin de Test", 10, 10);
            forestRepository.save(forest);
            System.out.println("✅ Forêt sauvegardée : " + forest.getName());

            // ----------------- 3. CRÉATION DE LA PLANTE (Venant de votre Test.java) -----------------
            Plant plant1 = new Plant("MyTomato", tomato);
            
            // On applique la logique de votre test (Stress hydrique)
            // "plant1.setWaterLevel(plant1.getWaterLevel() + 50);"
            plant1.setWaterLevel(tomato.getOptimalWaterNeeds() + 50); 
            
            // Calcul de l'état initial avant sauvegarde
            plant1.evaluateState();

            // IMPORTANT : Pour que la plante apparaisse dans la grille du Frontend,
            // il faut la placer dans la forêt (Position X, Y)
            // Supposons que votre classe Plant a un champ 'position' ou que Forest gère une liste.
            // Option A : Si Forest stocke la liste des IDs
            // Option B : Si vous utilisez l'API pour ajouter (recommandé en code métier, mais ici on fait du brut)
            
            // Simulation de l'ajout à la position (2, 2)
            // Note: Adaptez selon votre modèle 'Plant'. Avez-vous un champ x,y ou un objet Position ?
            // Exemple générique :
            // plant1.setPosition(new Position(2, 2)); 
            
            // SAUVEGARDE PLANTE
            plantRepository.save(plant1);

            // LIEN FORÊT <-> PLANTE
            // Il faut dire à la forêt qu'elle contient cette plante
            // Adaptez selon votre méthode : forest.addPlant(plant1) ou forest.getPlants().add(plant1)
            forest.getPlants().add(plant1); // Si c'est une liste d'objets
            forestRepository.save(forest);  // On resauvegarde la forêt mise à jour

            System.out.println("✅ Plante sauvegardée et ajoutée à la forêt : " + plant1.getName());
            System.out.println("🚀 DONNÉES INITIALISÉES AVEC SUCCÈS !");
        } else {
            System.out.println("ℹ️ La base de données contient déjà des forêts. Pas d'initialisation nécessaire.");
        }
    }
}