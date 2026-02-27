# Cahier des charges - GreenDesk

## 1. Contexte et vision

GreenDesk est une application de gestion agronomique orientée simulation, conçue pour suivre des plantes et espèces végétales, piloter des forêts (grilles de plantation), appliquer des effets et stimuli, et mesurer la performance opérationnelle via des indicateurs greenhouse (KPI, alertes, ROI).

## 2. Objectifs du produit

### 2.1 Objectifs métier

- Centraliser les opérations de gestion des espèces et des plantes.
- Simuler des environnements végétaux et leur évolution (saison, écosystème, maladies).
- Fournir des indicateurs de pilotage (santé, alertes, rentabilité).
- Faciliter les prises de décision via API exploitable et traçable.

### 2.2 Objectifs techniques

- Exposer une API REST claire et cohérente.
- Garantir une qualité logicielle mesurable (tests + couverture).
- Assurer une exécution locale simple et un mode Docker reproductible.
- Permettre l’évolution du modèle métier sans rupture de base.

## 3. Périmètre fonctionnel

### 3.1 Inclus (In Scope)

- CRUD espèces (`/api/species`).
- CRUD plantes (`/api/plants`, `/plants`).
- Gestion forêts + positionnement grille (`/api/forests`).
- Cycles de saisons par forêt (`/api/forests/{id}/season-cycle`).
- Catalogue et application d’effets (`/api/effects`, `/api/plants/*/effects`).
- Application de stimulus (`/api/stimuli`).
- Alertes plantes (`/plants/{plantId}/alerts`, `/alerts/{alertId}/ack`).
- Mesures capteurs (`/plants/{plantId}/sensor-readings`).
- Simulation écosystème (`/api/ecosystem/*`).
- KPIs greenhouse (`/api/greenhouse/*`).

### 3.2 Exclus (Out of Scope)

- Interface front complète multi-pages orientée produit final.
- Authentification/autorisation avancée (RBAC, OAuth).
- Multi-tenant isolé par organisation.
- Reporting BI externe natif (PowerBI/Tableau connectors).

## 4. Utilisateurs cibles

- **Opérateur serre**: suit l’état, agit via effets/stimuli.
- **Responsable agronomique**: pilote qualité, alertes, ROI.
- **Équipe technique**: intègre l’API, maintient l’application.

## 5. Exigences fonctionnelles détaillées

### 5.1 Gestion des espèces

- Créer, lister, consulter, modifier et supprimer une espèce.
- Valider les paramètres agronomiques essentiels.
- Conserver la cohérence entre espèces et plantes liées.

### 5.2 Gestion des plantes

- Créer une plante avec ou sans valeurs capteurs initiales.
- Obtenir l’état calculé (`state`) et un rapport détaillé (`status`).
- Comparer deux plantes (différences d’état/capteurs).
- Cloner une plante vers une forêt cible avec coordonnées.

### 5.3 Forêts et grille

- Créer une forêt de dimensions définies.
- Ajouter/supprimer une plante à une position (`x`,`y`).
- Empêcher les conflits de position (retour `409`).
- Appliquer les règles de diversité de plantation.

### 5.4 Saisons

- Initialiser un cycle saisonnier par forêt.
- Avancer de `n` mois et obtenir la saison active.
- Supprimer/consulter un cycle de saison.

### 5.5 Effets

- Lister le catalogue d’effets, filtrer les personnalisés.
- Créer un effet custom.
- Appliquer, lister et retirer des effets sur une plante.

### 5.6 Stimulus

- Publier un stimulus pour une forêt.
- Appliquer l’impact sur les plantes concernées.

### 5.7 Alertes

- Produire des alertes en fonction de seuils et état plante.
- Lister alertes actives/inactives.
- Acquitter une alerte.

### 5.8 Sensor readings

- Enregistrer des mesures horodatées.
- Lire l’historique complet ou filtré par intervalle.
- Lire la dernière mesure connue.

### 5.9 Simulation écosystème

- Exécuter un tick unitaire.
- Simuler plusieurs ticks.
- Obtenir l’état des cellules et des maladies.

### 5.10 KPIs Greenhouse

- Vue synthèse (`overview`).
- Impacts effets en temps réel (`live-effects`).
- Alertes consolidées (`alerts`).
- ROI global et par forêt (`roi`, `roi/forests`).
- Tick capteurs simulé (`sensor-stream/tick`).

## 6. Exigences non fonctionnelles

### 6.1 Performance

- Réponses API standard en temps compatible usage opérationnel local.
- Support de pagination/limitation par paramètres sur endpoints analytiques (`limit`, `hours`).

### 6.2 Fiabilité

- Gestion d’erreurs explicite (`400`, `404`, `409`).
- Conservation des données critiques en MongoDB.

### 6.3 Maintenabilité

- Structure en couches (controller/service/repository/entity).
- Documentation Markdown maintenue dans `docs/`.
- Tests et couverture automatisés via Gradle + JaCoCo.

### 6.4 Portabilité

- Exécution Linux/Mac/Windows via Gradle Wrapper.
- Exécution conteneurisée via Docker Compose.

### 6.5 Qualité logicielle

Seuils JaCoCo imposés:

- LINE >= 70%
- BRANCH >= 45%
- CLASS >= 90%

## 7. Contraintes techniques

- Java 21
- Spring Boot 3.3.3
- MongoDB
- Gradle
- OpenAPI/Swagger

## 8. Critères d’acceptation (macro)

### 8.1 API métier

- Tous les endpoints listés dans `docs/api/endpoints.md` sont appelables.
- Les cas d’erreur renvoient un statut HTTP cohérent.

### 8.2 Qualité

- `./gradlew clean check` passe sans erreur.
- Rapports tests et couverture générés.
- Seuils JaCoCo respectés.

### 8.3 Documentation

- Documentation accessible via `docs/index.md`.
- Exemples API exécutables fournis.
- Scénarios d’usage couverts (espèces, plantes, forêts, effets, KPI).

## 9. User stories prioritaires

1. En tant qu’opérateur, je crée une espèce puis une plante afin d’initialiser une culture.
2. En tant qu’opérateur, j’associe une plante à une forêt à une position donnée sans conflit.
3. En tant que responsable, je consulte alertes et KPI greenhouse pour détecter les dérives.
4. En tant qu’opérateur, j’applique un effet puis je mesure l’impact via capteurs.
5. En tant que responsable, je consulte le ROI global et le classement des forêts.

## 10. Risques et mitigations

- **Risque**: dérive de couverture sur modules complexes.
  - **Mitigation**: tests ciblés services/controllers avant merge.
- **Risque**: incohérences de données de configuration Mongo selon environnement.
  - **Mitigation**: standardiser variables d’environnement.
- **Risque**: duplication de chemins (`/plants` et `/api/plants`) source de confusion.
  - **Mitigation**: documenter clairement et privilégier `/api/plants` pour nouveaux usages.

## 11. Plan de livraison recommandé

### Lot 1 - Stabilisation qualité

- Renforcer tests sur `StimulusService` et `EcosystemController`.
- Uniformiser les formats d’erreur JSON.

### Lot 2 - Robustesse opérationnelle

- Externaliser toutes les configs sensibles via secrets/env.
- Ajouter healthchecks d’intégration.

### Lot 3 - Produit

- Ajouter authentification et rôles.
- Ajouter tableaux de bord utilisateur orientés métier.

## 12. Indicateurs de suivi projet

- Taux de succès pipeline CI.
- Couverture LINE/BRANCH/CLASS.
- Nombre d’alertes actives non acquittées.
- Temps moyen de résolution d’une alerte.
- Évolution ROI moyen par forêt.

## 13. Références internes

- [Documentation racine](index.md)
- [Architecture](architecture.md)
- [Référence API](api/overview.md)
- [Endpoints complets](api/endpoints.md)
- [Exemples API](api/examples.md)
- [Tests et qualité](testing.md)
- [Statut qualité](status.md)
