package org.example.repositories;

import org.example.entites.Effect;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EffectRepository extends MongoRepository<Effect, String> {
    Optional<Effect> findByName(String name);
    // CELUI QU'IL FAUT AJOUTER pour la Livraison 3
    // Il permet de faire : "Donne-moi tous les effets qui sont custom (ou pas)"
    List<Effect> findByIsCustom(boolean isCustom);
}
