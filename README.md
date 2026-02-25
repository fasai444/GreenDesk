
<div align="center">
  <h1>GreenDesk</h1>
  
  <a href="https://github.com/MisasoaRobison/GreenDesk/actions">
    <img src="https://img.shields.io/badge/Docs%20CI-GitHub%20Actions-blue?logo=github" alt="Docs CI" />
  </a>
  <a href="https://github.com/MisasoaRobison/GreenDesk/actions/workflows/docs-pages.yml">
    <img src="https://github.com/MisasoaRobison/GreenDesk/actions/workflows/docs-pages.yml/badge.svg" alt="Docs Deploy Status" />
  </a>
  <a href="docs/index.md">
    <img src="https://img.shields.io/badge/Documentation-Markdown-green" alt="Documentation" />
  </a>
  <a href="https://misasoarobison.github.io/GreenDesk/">
    <img src="https://img.shields.io/badge/Documentation-Live%20on%20GitHub%20Pages-2ea44f" alt="Documentation Live" />
  </a>
</div>

**README** : [README.md](README.md)


**Documentation en ligne (GitHub Pages)** : [https://misasoarobison.github.io/GreenDesk](https://misasoarobison.github.io/GreenDesk)

**Cahier des charges** : [docs/cahier-des-charges.md](docs/cahier-des-charges.md)

## Description

**GreenDesk** est une application **Spring Boot** + **MongoDB** dédiée à la gestion et à la simulation de plantes et d'espèces végétales. L'application permet de gérer les espèces, les plantes, d'évaluer leur état **(stress, santé)** et d'exposer ces informations via une **API REST**.

## Version

**v3.0** - *Livraison 3 (L3) implémentée*

## Technologies

- **Back-end**: *Java 21, Spring Boot 3.3.3*
- **Base de données**: *MongoDB (Atlas ou local)*
- **Build**: *Gradle 9.2.0*
- **Conteneurisation**: *Docker + Docker Compose*
- **Documentation API**: *Swagger/OpenAPI (springdoc-openapi)*
- **Tests**: *JUnit 5, SpringBootTest*

## Architecture

**Le projet met en pratique** :

- Architecture **Spring Boot** claire **(Controller / Service / Repository)**
- Utilisation de **MongoDB** avec **Spring Data**
- Modélisation métier **(espèces, plantes, forêts, saisons, effets)**
- Conteneurisation **Docker** complète avec **MongoDB** et **Mongo Express**

## Fonctionnalités implémentées

### Livraison 1 (L1) - Base

#### L1-F1: Gestion des espèces et plantes (CRUD + applications d'interventions)

##### Gestion des espèces

- Créer une espèce avec ses besoins optimaux :
  - Eau (waterNeeds)
  - Température (optimalTemperature)
  - Humidité (optimalHumidity)
  - Lumière (luxNeeds)
  - Taux de croissance (baseGrowthRate)
  - Production de graines (seedProductionRate)
- Lister toutes les espèces
- Récupérer une espèce par son nom
- Mettre à jour une espèce existante
- Supprimer une espèce
- Stockage persistant dans MongoDB

##### Gestion des plantes

- Créer une plante liée à une espèce existante
- Initialisation automatique des valeurs environnementales
- Lister toutes les plantes
- Récupérer une plante par son ID
- Consulter l'état d'une plante (HEALTHY, STRESSED, DORMANT, DISEASED)
- Mettre à jour une plante
- Supprimer une plante

##### Logique métier

- Calcul de l'état d'une plante en fonction :
  - Des besoins optimaux de l'espèce
  - Des valeurs environnementales actuelles
  - Stress calculé dynamiquement côté backend
- Interventions possibles : arroser, tailler, réduire la lumière
- Évolution des plantes avec prise en compte des effets appliqués

#### L1-F2: Simulation de la croissance et l'état d'une plante en fonction de son environnement

Cette fonctionnalité permet de simuler l’évolution d’une plante en fonction des conditions environnementales.

##### Principe

- Un environnement unique contient :
  - Température
  - Humidité
  - Luminosité (cycle jour/nuit)
  - Pluie
  - Horodatage

- À chaque heure simulée :
  - L’environnement évolue (variation climatique réaliste).
  - Chaque plante s’adapte aux nouvelles conditions.
  - Sa croissance et son état sont mis à jour.

### Livraison 2 (L2) - Forêts et effets

#### L2-F1: Gestion des forêts et saisons

- **Forêts** :
  - Créer une forêt avec dimensions (largeur x hauteur)
  - Placer des plantes dans une forêt à des positions (x, y)
  - R1 : validation de l'unicité des positions (409 Conflict si déjà occupée)
  - R2 : diversité génétique via `variationSeed` pour plantes de même espèce
  - Lister toutes les plantes d'une forêt
  - Supprimer une forêt

- **Saisons** :

  - 4 saisons prédéfinies : WINTER, SPRING, SUMMER, AUTUMN
  - Modificateurs environnementaux par saison
  - Cycles de saisons pour chaque forêt
  - Progression automatique des saisons
  - Récupérer la saison actuelle d'une forêt

#### L2-F2: Système d'effets

- **Effets prédéfinis** :

  - **Shade** (Ombre) : réduit la lumière de 30%
  - **Fertilizer** (Engrais) : augmente la croissance de 20%
  - **Extra Watering** (Arrosage supplémentaire) : augmente l'eau de 15%
  - **Heating** (Chauffage) : augmente la température de 3 °C
  
- **Gestion des effets** :

  - Appliquer un effet à une plante
  - Lister les effets actifs d'une plante
  - Retirer un effet d'une plante
  - Calcul automatique des modificateurs lors de l'évolution
  - Intégration dans le calcul de l'état des plantes

### Livraison 3 (L3) - Effets personnalisés et comparaison par stimulus

#### L3-F1: Effets personnalisés

- Création d'effets personnalisables via API
- Filtrage des effets personnalisés avec `?custom=true`
- Réutilisation du mécanisme d'attachement effet → plante

#### L3-F2: Stimulus par forêt + comparaison

- Envoi de stimulus à une forêt (`HEATWAVE`, `RAIN`, ...)
- Clonage d'une plante dans une forêt cible
- Rapport détaillé d'état (`/plants/{id}/status`)
- Comparaison de deux plantes (`/plants/compare`)

Points d'entrée ajoutés/validés pour L3 :

- `POST /api/effects`
- `GET /api/effects?custom=true`
- `POST /api/stimuli`
- `POST /plants/{plantId}/clone?forestId=...&x=...&y=...`
- `GET /plants/{plantId}/status`
- `GET /plants/compare?leftId=...&rightId=...`

Validation stimulus (`POST /api/stimuli`) :

- `forestId` requis
- `type` requis
- `durationHours > 0` requis
- Retour `400` avec un message d'erreur explicite en cas de payload invalide

## Structure du projet

```
GreenDesk/
├── src/main/java/org/example/
│   ├── controllers/
│   │   ├── HomeController.java           # Redirection racine vers l'interface Web
│   │   ├── PlantController.java          # Points d'entrée REST pour les plantes
│   │   ├── SpeciesController.java        # Points d'entrée REST pour les espèces
│   │   ├── ForestController.java         # Points d'entrée REST pour les forêts
│   │   ├── SeasonController.java         # Points d'entrée REST pour les saisons
│   │   ├── ForestSeasonController.java   # Gestion saisons des forêts
│   │   └── EffectController.java         # Points d'entrée REST pour les effets
│   │
│   ├── entities/
│   │   ├── Plant.java                    # Modèle plante + logique d'état
│   │   ├── Species.java                  # Modèle espèce (besoins optimaux)
│   │   ├── Forest.java                   # Modèle forêt + positions
│   │   ├── Season.java                   # Modèle saison + modificateurs
│   │   ├── SeasonType.java               # Enum des saisons
│   │   ├── SeasonCycle.java              # Cycle de saisons pour forêt
│   │   ├── Effect.java                   # Modèle effet
│   │   ├── PlantEffect.java              # Association plante-effet
│   │   ├── PlantState.java               # Enum des états (HEALTHY, STRESSED…)
│   │   ├── EnvironmentData.java          # Données environnementales
│   │   ├── SensorReading.java            # Lecture de capteurs
│   │   └── Intervention.java             # Actions possibles sur une plante
│   │
│   ├── repositories/
│   │   ├── PlantRepository.java          # Accès MongoDB pour Plant
│   │   ├── SpeciesRepository.java        # Accès MongoDB pour Species
│   │   ├── ForestRepository.java         # Accès MongoDB pour Forest
│   │   ├── SeasonCycleRepository.java    # Accès MongoDB pour SeasonCycle
│   │   ├── EffectRepository.java         # Accès MongoDB pour Effect
│   │   ├── PlantEffectRepository.java    # Accès MongoDB pour PlantEffect
│   │   └── SensorReadingRepository.java  # Accès MongoDB pour SensorReading
│   │
│   ├── services/
│   │   ├── PlantService.java            # Logique métier des plantes
│   │   ├── SpeciesService.java          # Logique métier des espèces
│   │   ├── ForestService.java            # Logique métier des forêts (R1/R2)
│   │   ├── SeasonService.java            # Logique métier des saisons
│   │   ├── EffectService.java            # Logique métier des effets
│   │   ├── EnvironmentService.java      # Évolution de l'environnement
│   │   └── Simulation.java               # Simulation manuelle ou horaire
│   │
│   ├── GreenDesk.java                    # Classe principale Spring Boot
│   └── Main.java                         # Point d'entrée alternatif
│
├── src/main/resources/
│   └── application.properties            # Configuration MongoDB & serveur
│
├── src/test/java/org/example/
│   ├── TestPlantServices.java            # Tests des services plantes
│   ├── TestSpeciesServices.java          # Tests des services espèces
│   ├── TestSimulationEnvironment.java    # Tests de simulation
│   ├── TestForestAndSeasons.java         # Tests L2-F1 (8 tests)
│   └── TestEffects.java                  # Tests L2-F2 (5 tests)
│
├── Dockerfile                            # Multi-stage build pour conteneur app
├── docker-compose.yml                    # Orchestration 3 services
├── .dockerignore                         # Optimisation build Docker
├── docs/
│   └── docker.md                         # Documentation Docker complète
├── scripts/
│   └── test-docker.sh                    # Script de test automatisé
├── build.gradle                          # Configuration Gradle
└── README.md                             # Ce fichier
```

## Installation et lancement

### Prérequis

- Java 21+
- Gradle 9.2.0+
- MongoDB Atlas ou MongoDB local

### Option 1: Lancement local (sans Docker)

1. **Cloner le dépôt**

```bash
git clone <repo-url>
cd GreenDesk
```

2. **Configurer MongoDB**

**Éditer `src/main/resources/application.properties`**:

```properties
# Local par défaut
spring.data.mongodb.uri=mongodb://localhost:27017/greendesk
spring.data.mongodb.database=greendesk

# Optionnel (exemple Atlas via variables d'environnement)
# SPRING_DATA_MONGODB_URI=mongodb+srv://USER:PASSWORD@cluster.mongodb.net/greendesk
# SPRING_DATA_MONGODB_DATABASE=greendesk

server.port=8080
```

3. **Lancer l'application**

```bash
./gradlew bootRun
```

L'application démarre sur **http://localhost:8080**.

Accès recommandés :

- **Accueil (parcours utilisateur)** : http://localhost:8080/home.html
- **Tableau de bord (cockpit)** : http://localhost:8080/dashboard.html
- **Simulation** : http://localhost:8080/index.html
- **Swagger UI** : http://localhost:8080/swagger-ui.html

Si le port `8080` est déjà occupé, lancer sur un autre port :

```bash
./gradlew bootRun --args='--server.port=8081'
```

### Option 2: Lancement avec Docker (recommandé)

1. **Prérequis Docker**

- **Docker Engine** installé
- **Docker Compose** installé

2. **Lancer tous les services**

```bash
docker compose up -d
```

**Cette commande lance** :

- **app**: Application **Spring Boot (port 8080)**
- **mongodb**: Base de données **MongoDB (port 27017)**
- **mongo-express**: Interface web **MongoDB (port 8081)**

3. **Vérifier les services**

```bash
# Statut des conteneurs
docker compose ps

# Logs de l'application
docker compose logs -f app

# Script de test automatisé
./scripts/test-docker.sh
```

4. **Accès aux services**

- **Application API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs
- **Mongo Express**: http://localhost:8081 (admin/admin)
- **MongoDB**: mongodb://localhost:27017

5. **Arrêter les services**

```bash
# Arrêter les conteneurs
docker compose down

# Arrêter et supprimer les données
docker compose down -v
```

Voir [docs/docker.md](docs/docker.md) pour la documentation Docker complète.

## Utilisation de l'API

### Documentation interactive

Accéder à **Swagger UI** pour une documentation interactive complète :

- **URL**: http://localhost:8080/swagger-ui.html
- Tous les points d'entrée y sont documentés avec possibilité de test direct

### Exemples d'utilisation

#### Gestion des espèces

**Créer une espèce**

```bash
curl -X POST http://localhost:8080/api/species \
-H "Content-Type: application/json" \
-d '{
  "name": "Tomato",
  "optimalWaterNeeds": 200,
  "optimalTemperature": 22,
  "optimalHumidity": 60,
  "optimalLuxNeeds": 1500,
  "baseGrowthRate": 1.5,
  "seedProductionRate": 0.4
}'
```

**Lister toutes les espèces**

```bash
curl http://localhost:8080/api/species
```

**Récupérer une espèce par nom**

```bash
curl http://localhost:8080/api/species/Tomato
```

**Mettre à jour une espèce**

```bash
curl -X PUT http://localhost:8080/api/species/SPECIES_ID \
-H "Content-Type: application/json" \
-d '{"optimalWaterNeeds": 250}'
```

**Supprimer une espèce**

```bash
curl -X DELETE http://localhost:8080/api/species/SPECIES_ID
```

#### Gestion des plantes

**Créer une plante**

```bash
curl -X POST "http://localhost:8080/plants/create?name=Tomato_Plant_1&speciesId=SPECIES_ID"
```

**Lister toutes les plantes**

```bash
curl http://localhost:8080/plants
```

**Récupérer une plante par ID**

```bash
curl http://localhost:8080/plants/PLANT_ID
```

**Consulter l'état d'une plante**

```bash
curl http://localhost:8080/plants/PLANT_ID/state
```

**Réponse possible**: `HEALTHY`, `STRESSED`, `DORMANT`, `DISEASED`

**Mettre à jour une plante**

```bash
curl -X PUT "http://localhost:8080/plants/PLANT_ID?water=220&temperature=23"
```

**Supprimer une plante**

```bash
curl -X DELETE http://localhost:8080/plants/PLANT_ID
```

#### Gestion des forêts

**Créer une forêt**

```bash
curl -X POST http://localhost:8080/api/forests \
-H "Content-Type: application/json" \
-d '{
  "name": "Forest1",
  "width": 10,
  "height": 10
}'
```

**Ajouter une plante à une forêt**

```bash
curl -X POST "http://localhost:8080/api/forests/FOREST_ID/plants/PLANT_ID?x=3&y=5"
```

**Lister les plantes d'une forêt**

```bash
curl http://localhost:8080/api/forests/FOREST_ID/plants
```

**Supprimer une forêt**

```bash
curl -X DELETE http://localhost:8080/api/forests/FOREST_ID
```

#### Gestion des saisons

**Lister toutes les saisons disponibles**

```bash
curl http://localhost:8080/api/seasons
```

**Récupérer une saison par type**

```bash
curl http://localhost:8080/api/seasons/WINTER
```

**Obtenir la saison actuelle d'une forêt**

```bash
curl http://localhost:8080/api/forests/FOREST_ID/current-season
```

**Faire progresser la saison d'une forêt**

```bash
curl -X POST http://localhost:8080/api/forests/FOREST_ID/advance-season
```

#### Gestion des effets

**Lister tous les effets disponibles**

```bash
curl http://localhost:8080/api/effects
```

**Récupérer un effet par nom**

```bash
curl http://localhost:8080/api/effects/Shade
```

**Appliquer un effet à une plante**

```bash
curl -X POST http://localhost:8080/api/plants/PLANT_ID/effects/EFFECT_ID
```

**Lister les effets actifs d'une plante**

```bash
curl http://localhost:8080/api/plants/PLANT_ID/effects
```

**Retirer un effet d'une plante**

```bash
curl -X DELETE http://localhost:8080/api/plants/effects/PLANT_EFFECT_ID
```

## Tests

**Le projet inclut une suite de tests complète**:

### Exécution des tests

```bash
# Tous les tests
./gradlew test

# Tests spécifiques
./gradlew test --tests TestForestAndSeasons
./gradlew test --tests TestEffects
```

Les tests utilisent désormais un MongoDB embarqué (profil `application-test.properties`) : aucune dépendance Atlas n'est requise pour exécuter la suite localement.

### Suite de tests

- **TestPlantServices.java**: Tests des services de gestion des plantes
- **TestSpeciesServices.java**: Tests des services de gestion des espèces
- **TestSimulationEnvironment.java**: Tests de la simulation environnementale
- **TestForestAndSeasons.java**: **8 tests** pour **L2-F1**
  - Création de forêt
  - Ajout de plantes avec validation **R1 (unicité position)**
  - Diversité génétique **R2 (variationSeed)**
  - Gestion des saisons et cycles
- **TestEffects.java**: **5 tests** pour **L2-F2**
  - Application d'effets
  - Calcul des modificateurs
  - Intégration dans l'évolution des plantes

**Couverture actuelle**: la base inclut désormais une suite étendue de tests unitaires, d'intégration et de contrôleurs (plusieurs centaines de tests automatisés).

### Résultats attendus

**Tous les tests doivent passer**:

```
BUILD SUCCESSFUL
```

## Architecture et fonctionnement

### Modèles principaux

- **Species**: Définit les besoins idéaux d'une plante et ses taux de croissance/production
- **Plant**: Liée à une espèce, possède ses valeurs actuelles et son stressIndex
- **Forest**: Contient des plantes avec leurs positions **(x, y)**
- **Season**: Modificateurs environnementaux selon la saison
- **SeasonCycle**: Gestion du cycle des saisons pour une forêt
- **Effect**: Modificateurs appliqués à une plante
- **PlantEffect**: Association entre une plante et un effet actif
- **PlantState**: Enum représentant l'état calculé dynamiquement selon l'écart aux besoins optimaux

### Services

- **SpeciesService**: Gestion **CRUD** des espèces
- **PlantService**: Gestion des plantes + calcul d'état + évolution **(avec effets)**
- **ForestService**: Gestion des forêts + validation **R1/R2**
- **SeasonService**: Catalogue des saisons + gestion des cycles
- **EffectService**: Catalogue des effets + calcul des modificateurs totaux
- **EnvironmentService**: Évolution de l'environnement des plantes
- **Simulation**: Simulation automatique ou manuelle

### Controllers

- **HomeController**: Redirection de `/` vers l'interface Web
- **SpeciesController**: Points d'entrée **REST** pour les espèces
- **PlantController**: Points d'entrée **REST** pour les plantes
- **ForestController**: Points d'entrée **REST** pour les forêts
- **SeasonController**: Points d'entrée **REST** pour les saisons
- **ForestSeasonController**: Gestion des saisons des forêts
- **EffectController**: Points d'entrée **REST** pour les effets

### Repositories

- Tous héritent de `MongoRepository` pour l'accès à **MongoDB**
- Méthodes de recherche personnalisées selon les besoins

## Règles métier implémentées

### R1: Unicité des positions dans une forêt

- Une position **(x, y)** ne peut être occupée que par une seule plante
- **HTTP 409 Conflict** si position déjà occupée

### R2: Diversité génétique

- Chaque plante reçoit un `variationSeed` unique
- Permet de différencier des plantes de même espèce
- Utilisé pour créer de la diversité dans l'évolution

### Calcul de l'état des plantes

1. Comparaison des valeurs actuelles vs besoins optimaux de l'espèce
2. Application des modificateurs de saison **(si forêt)**
3. Application des modificateurs d'effets actifs
4. Calcul du stress index
5. **Détermination de l'état**: HEALTHY, STRESSED, DORMANT, DISEASED

### Évolution des plantes

- Prise en compte des effets actifs
- Modification de la croissance selon les modificateurs
- Impact sur l'état de santé

## Documentation Docker

Voir [docs/docker.md](docs/docker.md) pour:

- Architecture **Docker** complète
- Configuration des services
- Commandes utiles
- Gestion des volumes et réseaux
- Dépannage

## Validation L3

### Livraison 3 (L3) - Fonctionnalités L3-F1 et L3-F2

#### L3-F1 : Simulation de la propagation de maladie dans l'écosystème (la forêt) 

- Cette fonctionnalité simule la propagation d’une maladie végétale au sein d’une forêt selon une logique locale : l’état d’une plante dépend de l’état de ses voisines directes (s'inspire du modèle de Schelling).

##### Principe 

- La forêt est modélisée comme une grille d'EcosystemCell :
  - Une cellule peut contenir une plante ou être vide.
  - Une plante peut être saine ou malade.
  - Chaque cellule observe ses voisines (adjacentes + diagonales).
  - Les cellules vides ne sont pas prises en compte dans les calculs de ratio.

- À chaque tick :
  - La maladie progresse chez les plantes infectées (progress()).
  - Les décisions d’infection ou de guérison sont évaluées.
  - Les changements sont appliqués simultanément (évite les effets en chaîne).

##### Infection : plus qu’un simple nombre de voisins malades

- Une plante saine peut devenir infectée si :
  - Elle contient effectivement une plante.
  - Elle n’est pas déjà malade.
  - Elle a au moins un voisin avec plante.
  - La proportion de voisins infectés parmi les voisins contenant une plante dépasse un seuil.
  - Une maladie dominante est identifiée parmi les voisines (sévérité moyenne la plus élevée).
  - Le seuil utilisé est celui défini par la maladie dominante (getInfectionThreshold()).
  - L’infection dépend donc de :
    - Du ratio de voisins infectés
    - Du type de maladie présente
    - Du seuil propre à cette maladie
    - De la présence effective de plantes autour

##### Guérison : condition symétrique mais indépendante

- Une plante malade peut guérir si :
  - Elle contient une plante.
  - Elle est actuellement infectée.
  - Elle possède au moins un voisin contenant une plante. 
  - La proportion de voisins sains dépasse le seuil de guérison.
  - Le seuil utilisé est celui défini par la maladie actuelle (getRecoveryThreshold()).
  - La guérison dépend de :
    - Du ratio de voisins sains
    - Du seuil propre à la maladie en cours
    - De la densité locale de plantes

**Lancer un tick unique** 

```bash
curl -X POST http://localhost:8080/api/ecosystem/tick
```

**Simuler plusieurs ticks**

```bash
curl -X POST http://localhost:8080/api/ecosystem/simulate/{n}
```
**Consulter l'état des cellules:**
  - Coordonnées [x,y]
  - ID de la plante
  - Maladie active ou `Healthy`
  - Niveau de sévérité

```bash
curl http://localhost:8080/api/ecosystem/cells
```

**Exemple de script complet**
```bash
#!/bin/bash


# 1. Récupérer l'ID de l'espèce Tomato qui est déjà présent en base de données
SPECIES_ID=$(curl -s http://localhost:8080/api/species | jq -r '.[] | select(.name=="Tomato") | .id')
echo "ID de l'espèce Tomato: $SPECIES_ID"

# Créer une forêt
FOREST_NAME="SimulationForest"
FOREST_WIDTH=10
FOREST_HEIGHT=10

FOREST_ID=$(curl -s -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$FOREST_NAME\", \"width\":$FOREST_WIDTH, \"height\":$FOREST_HEIGHT}" \
  | jq -r '.id')

echo "ID de la forêt: $FOREST_ID"

# créer des plantes et les ajouter dans la forêt
for i in $(seq 1 10); do
  PLANT_NAME="Tomato_$i"
  PLANT_ID=$(curl -s -X POST "http://localhost:8080/plants/create?name=$PLANT_NAME&speciesId=$SPECIES_ID" | jq -r '.id')
  echo "Plante $i créée: $PLANT_ID"

  X=$(( RANDOM % FOREST_WIDTH ))
  Y=$(( RANDOM % FOREST_HEIGHT ))

  RESPONSE=$(curl -s -X POST "http://localhost:8080/api/forests/$FOREST_ID/plants" \
    -H "Content-Type: application/json" \
    -d "{\"plantId\":\"$PLANT_ID\",\"x\":$X,\"y\":$Y}")
  
  echo "Ajout plante $i à ($X,$Y)"
done

# appliquer la simulation à cette forêt (le service initialise aussi l'écosystème)
INIT_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/ecosystem/simulate/$FOREST_ID/0")
echo "Écosystème initialisé: $INIT_RESPONSE"

for tick in $(seq 1 $NUM_TICKS); do
  echo "--------------------------------------"
  echo "Tick $tick..."
  
  TICK_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/ecosystem/tick/$FOREST_ID")
  echo "$TICK_RESPONSE"

  # Récupérer l'état détaillé des cellules
  CELLS=$(curl -s "http://localhost:8080/api/ecosystem/cells/$FOREST_ID" | jq -r '.[]')
  echo "État des cellules après tick $tick:"
  echo "$CELLS"
done 

echo "suppression des plantes créées"
DELETE_RESPONSE=$(curl -s -X DELETE "http://localhost:8080/plants")
echo "$DELETE_RESPONSE"

```

#### L3-F2 : Système d'effets personnalisés et stimuli environnementaux

- Cette fonctionnalité offre à l’utilisateur un contrôle avancé sur ses plantes et sur l’environnement de la forêt. Elle combine :
  - La création d’effets personnalisés appliqués aux plantes
  - La simulation de stimuli climatiques à l’échelle d’une forêt
  - L’analyse comparative des réactions physiologiques des plantes

**Création d'effets personnalisés :** implémentation d'un système permettant de définir des modificateurs de température, d'eau et de stress index uniques.
**Persistance Différenciée :** Utilisation d'un attribut **isCustom (boolean)** pour séparer les effets natifs du simulateur des créations de l'utilisateur.
**Filtrage API :** Mise à jour des points d'entrée de consultation pour permettre l'affichage exclusif des effets personnalisés dans le tableau de bord utilisateur.

### 1. Stimulus de Masse par Forêt

Au lieu de cibler chaque plante individuellement, le système peut désormais appliquer un événement **(ex: HEATWAVE, RAIN)** à l'ensemble des plantes rattachées à un forestId spécifique.

**Logique :** Le stimulus génère automatiquement un effet temporaire appliqué à chaque membre de la forêt ciblée.

**Lancer une canicule sur une forêt :**

```bash
curl -X POST http://localhost:8080/api/stimuli \
-H "Content-Type: application/json" \
-d '{
  "type": "HEATWAVE",
  "forestId": "ID_FORET_ALPHA",
  "intensity": 45.0,
  "durationHours": 12
}'
```
### 2. Protocole de Clonage Scientifique

Pour garantir une comparaison rigoureuse, une fonction de clonage a été développée.

**Réplication Totale :** Copie exacte des attributs **(taille, niveaux, état)**.

**Génétique (Seed) :** le **variationSeed** est également copié, assurant que l'original et le clone réagissent de manière identique si les conditions sont égales.

**Cloner une plante (Témoin) :**

```bash
curl -X POST "http://localhost:8080/plants/ID_PLANTE/clone?forestId=ID_FORET_BETA&x=2&y=2"
```
### 3. Rapport d'État Détaillé (/status)

Un nouvel endpoint de diagnostic affiche le **Stress Index** calculé, les données en temps réel des **Capteurs (Eau, Température, Humidité, Lux)** et le contexte environnemental.

**Consulter le diagnostic :**

```bash
curl http://localhost:8080/plants/ID_PLANTE/status
```

Le payload `/status` inclut notamment :

- `plantId`, `name`, `plantState`, `stressIndex`, `heightCm`
- `stressDetails` (`waterStress`, `tempStress`, `humidityStress`, `lightStress`)
- `sensors` (`waterLevel`, `temperature`, `humidity`, `lux`)
- `activeEffects`
- `latestStimuli` et alias `recentStimuli`
- `forestId`, `x`, `y` et `forest.position`

**Comparer deux plantes (clone vs clone) :**

```bash
curl "http://localhost:8080/plants/compare?leftId=PLANT_ID_A&rightId=PLANT_ID_B"
```

Le bloc `comparison` retourne les deltas observables (`stressIndexDelta`, `heightCmDelta`, `sensorDelta`, `stateChanged`).
## III. ARCHITECTURE TECHNIQUE ET API

**Voici les points d'entrée ajoutés pour cette livraison** :

**POST :** `/api/effects`

**Description :** Enregistre un nouvel effet personnalisé créé par l'utilisateur.

**POST :** `/api/stimuli`

**Description :** Déclenche un événement climatique **(ex: canicule ou pluie)** sur une forêt entière.

**POST :** `/plants/{id}/clone`

**Description :** Duplique une plante spécifique vers une forêt de test pour servir de témoin.

**GET :** `/plants/{id}/status`

**Description :** Affiche le rapport de diagnostic complet **(stress, capteurs, état de santé)**.

## IV. PROTOCOLE DE VALIDATION (SCÉNARIO)

**Préparation :** Créer une plante dans la **Forêt Alpha**.

**Clonage :** Dupliquer cette plante dans la Forêt Beta.

**Simulation :** Envoyer un stimulus **HEATWAVE** sur la **Forêt Alpha**.

**Comparaison :** Consulter le /status des deux plantes. La plante **"Alpha"** doit présenter un stress thermique élevé, validant ainsi l'impact localisé du stimulus et l'efficacité du système de comparaison.

### Script de démo L3 (automatisé)

Un script prêt à l'emploi exécute tout le protocole de validation en une seule commande :

```bash
./scripts/demo-l3.sh
```

Variables utiles :

- `BASE` : URL de l'API (par défaut `http://localhost:8080`)

Exemple sur un autre port :

```bash
BASE=http://localhost:8081 ./scripts/demo-l3.sh
```

Le script :

- crée une espèce de test,
- crée deux forêts,
- crée une plante A dans la forêt A,
- clone cette plante vers la forêt B,
- applique un stimulus `HEATWAVE` à la forêt A,
- récupère `/plants/{id}/status` pour A et B,
- exécute `/plants/compare` et affiche un résumé lisible avec verdict.

## Contribution

**Pour contribuer au projet** :

1. **Fork** le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une **Pull Request**

## Licence

Ce projet est un projet académique développé dans le cadre d'une formation.

## Contact

Pour toute question ou suggestion, veuillez ouvrir une issue sur le dépôt **GitHub**.

---



**Dernière mise à jour**: Février 2026
**Statut**: Production-ready (API, UI et Docker)
