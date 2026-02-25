package org.example.repositories;

import org.example.entities.plant.Plant;
import org.example.entities.plant.PlantState;
import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository amélioré pour l'entité Plant
 * Gère les recherches complexes pour le simulateur et le dashboard
 */
@Repository
public interface PlantRepository extends MongoRepository<Plant, String> {

    // --- RECHERCHES BASIQUES (Barre de recherche) ---

    /**
     * Recherche partielle sur le nom de la plante (ex: "Tomato" trouve
     * "Tomato_Plant_1")
     * Indispensable pour la barre de recherche du Frontend.
     */
    List<Plant> findByNameContainingIgnoreCase(String name);

    // --- FILTRES PAR ÉTAT DE SANTÉ (Alertes Dashboard) ---

    /**
     * Trouver les plantes par état actuel (ex: toutes les plantes STRESSED ou
     * DISEASED)
     */
    List<Plant> findByPlantState(PlantState plantState);

    /**
     * Trouver les plantes en danger critique (Stress élevé)
     */
    List<Plant> findByStressIndexGreaterThan(double stressIndexThreshold);

    /**
     * Trouver les plantes qui ont soif (Niveau d'eau bas)
     */
    List<Plant> findByWaterLevelLessThan(double waterLevelThreshold);

    // --- LIEN AVEC LES ESPÈCES ---

    /**
     * Trouver toutes les plantes d'une espèce donnée par son NOM
     */
    List<Plant> findBySpeciesName(String speciesName);

    /**
     * AJOUTÉ : Trouver par ID d'espèce (Plus robuste si le nom change)
     * Utile quand on clique sur une espèce dans la page "Species" pour voir ses
     * plantes.
     */
    List<Plant> findBySpeciesId(String speciesId);

    // --- LIEN AVEC LES EFFETS (L2-F2) ---

    /**
     * AJOUTÉ : Trouver les plantes qui ont des effets actifs (Engrais, Ombre...)
     * Utile pour visualiser quelles plantes sont sous traitement.
     */
    // List<Plant> findByActiveEffectsIsNotEmpty();

    // --- DASHBOARD & TRI ---

    /**
     * AJOUTÉ : Récupérer les 10 dernières plantes créées (Trié par ID décroissant)
     * C'est CETTE méthode qui va remplir la section "Recent Plants" du Dashboard !
     */
    List<Plant> findTop10ByOrderByIdDesc();

    /**
     * Plantes triées par stress décroissant (Les plus malades en premier)
     * Utile pour une vue "Urgence".
     */
    List<Plant> findAllByOrderByStressIndexDesc();

    // --- STATISTIQUES (Compteurs) ---

    /**
     * Compter le nombre de plantes par état (ex: Combien de "HEALTHY" ?)
     */
    long countByPlantState(PlantState plantState);

    /**
     * Compter le nombre de plantes par espèce
     */
    long countBySpeciesName(String speciesName);

    // --- AJOUT POUR L3-F2 (Stimulus par forêt) ---

    /**
     * Trouve toutes les plantes d'une forêt spécifique. [cite: 76, 85]
     * Nécessaire pour appliquer un stimulus à toute la forêt et comparer les états.
     * [cite: 144, 164]
     */
    List<Plant> findByForestId(String forestId);
}