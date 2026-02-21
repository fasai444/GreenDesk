package org.example.repositories;

import org.example.entites.species.Species;
import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository amélioré pour l'entité Species
 * Inspiré des bonnes pratiques du projet GreenDesk, adapté à MongoDB
 */
@Repository
public interface SpeciesRepository extends MongoRepository<Species, String> {

    /**
     * Trouver une espèce par son nom exact (pour éviter les doublons lors de la création)
     */
    Optional<Species> findByName(String name);

    /**
     * Trouver les espèces par plage de besoins en eau
     */
    List<Species> findByOptimalWaterNeedsBetween(double min, double max);

    /**
     * Trouver les espèces adaptées à une plage de température
     */
    List<Species> findByOptimalTemperatureBetween(double min, double max);

    /**
     * Trouver les espèces adaptées à une plage d'humidité
     */
    List<Species> findByOptimalHumidityBetween(double min, double max);

    /**
     * Trouver les espèces adaptées à une plage de lumière (lux)
     */
    List<Species> findByOptimalLuxNeedsBetween(double min, double max);

    /**
     * Trouver les espèces qui tolèrent une température maximale donnée
     * (utile pour filtrer les espèces résistantes au chaud)
     */
    List<Species> findByOptimalTemperatureLessThanEqual(double maxTemp);

    /**
     * Trouver les espèces qui supportent beaucoup de lumière
     */
    List<Species> findByOptimalLuxNeedsGreaterThanEqual(double minLux);

    /**
     * Trouver les espèces à croissance rapide
     */
    List<Species> findByBaseGrowthRateGreaterThanEqual(double minGrowthRate);

    /**
     * Toutes les espèces triées par besoin en eau croissant
     * (pratique pour afficher les plus économes en premier)
     */
    List<Species> findAllByOrderByOptimalWaterNeedsAsc();

    /**
     * Toutes les espèces triées par taux de croissance décroissant
     */
    List<Species> findAllByOrderByBaseGrowthRateDesc();
}