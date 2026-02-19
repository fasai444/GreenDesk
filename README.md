# GreenDesk

## Description

GreenDesk est une application Spring Boot + MongoDB pour la gestion et simulation de plantes et d'especes vegetales. L'application permet de gerer des especes vegetales, des plantes, d'evaluer leur etat (stress, sante) et d'exposer ces informations via une API REST.

## Version
**v2.0** - Livraison 2 (L2) implementee

## Technologies

- **Backend**: Java 21, Spring Boot 3.3.3
- **Base de donnees**: MongoDB (Atlas ou local)
- **Build**: Gradle 9.2.0
- **Conteneurisation**: Docker + Docker Compose
- **API Documentation**: Swagger/OpenAPI (springdoc-openapi)
- **Tests**: JUnit 5, SpringBootTest

## Architecture

Le projet met en pratique :
- Architecture Spring Boot claire (Controller / Service / Repository)
- Utilisation de MongoDB avec Spring Data
- Modelisation metier (especes, plantes, forets, saisons, effets)
- Conteneurisation Docker complete avec MongoDB et Mongo Express

## Fonctionnalites implementees

### Livraison 1 (L1) - Base

#### Gestion des especes
- Creer une espece avec ses besoins optimaux :
  - Eau (waterNeeds)
  - Temperature (optimalTemperature)
  - Humidite (optimalHumidity)
  - Lumiere (luxNeeds)
  - Taux de croissance (baseGrowthRate)
  - Production de graines (seedProductionRate)
- Lister toutes les especes
- Recuperer une espece par son nom
- Mettre a jour une espece existante
- Supprimer une espece
- Stockage persistant dans MongoDB

#### Gestion des plantes
- Creer une plante liee a une espece existante
- Initialisation automatique des valeurs environnementales
- Lister toutes les plantes
- Recuperer une plante par son ID
- Consulter l'etat d'une plante (HEALTHY, STRESSED, DORMANT, DISEASED)
- Mettre a jour une plante
- Supprimer une plante

#### Logique metier
- Calcul de l'etat d'une plante en fonction :
  - Des besoins optimaux de l'espece
  - Des valeurs environnementales actuelles
  - Stress calcule dynamiquement cote backend
- Interventions possibles : arroser, tailler, reduire la lumiere
- Evolution des plantes avec prise en compte des effets appliques

### Livraison 2 (L2) - Forets et Effets

#### L2-F1: Gestion des forets et saisons
- **Forets** :
  - Creer une foret avec dimensions (largeur x hauteur)
  - Placer des plantes dans une foret a des positions (x, y)
  - R1 : Validation unicite des positions (409 Conflict si deja occupee)
  - R2 : Diversite genetique via variationSeed pour plantes de meme espece
  - Lister toutes les plantes d'une foret
  - Supprimer une foret

- **Saisons** :
  - 4 saisons predefinies : WINTER, SPRING, SUMMER, AUTUMN
  - Modificateurs environnementaux par saison
  - Cycles de saisons pour chaque foret
  - Progression automatique des saisons
  - Recuperer la saison actuelle d'une foret

#### L2-F2: Systeme d'effets
- **Effets predefinis** :
  - **Shade** (Ombre) : Reduit lumiere de 30%
  - **Fertilizer** (Engrais) : Augmente croissance de 20%
  - **Extra Watering** (Arrosage supplementaire) : Augmente eau de 15%
  - **Heating** (Chauffage) : Augmente temperature de 3 degres C
  
- **Gestion des effets** :
  - Appliquer un effet a une plante
  - Lister les effets actifs d'une plante
  - Retirer un effet d'une plante
  - Calcul automatique des modificateurs lors de l'evolution
  - Integration dans le calcul de l'etat des plantes

## Structure du projet

```
GreenDesk/
├── src/main/java/org/example/
│   ├── controllers/
│   │   ├── HomeController.java           # Redirection racine vers Swagger
│   │   ├── PlantController.java          # Endpoints REST pour les plantes
│   │   ├── SpeciesController.java        # Endpoints REST pour les especes
│   │   ├── ForestController.java         # Endpoints REST pour les forets
│   │   ├── SeasonController.java         # Endpoints REST pour les saisons
│   │   ├── ForestSeasonController.java   # Gestion saisons des forets
│   │   └── EffectController.java         # Endpoints REST pour les effets
│   │
│   ├── entites/
│   │   ├── Plant.java                    # Modele plante + logique d'etat
│   │   ├── Species.java                  # Modele espece (besoins optimaux)
│   │   ├── Forest.java                   # Modele foret + positions
│   │   ├── Season.java                   # Modele saison + modificateurs
│   │   ├── SeasonType.java               # Enum des saisons
│   │   ├── SeasonCycle.java              # Cycle de saisons pour foret
│   │   ├── Effect.java                   # Modele effet
│   │   ├── PlantEffect.java              # Association plante-effet
│   │   ├── PlantState.java               # Enum des etats (HEALTHY, STRESSED…)
│   │   ├── EnvironmentData.java          # Donnees environnementales
│   │   ├── SensorReading.java            # Lecture de capteurs
│   │   └── Intervention.java             # Actions possibles sur une plante
│   │
│   ├── repositories/
│   │   ├── PlantRepository.java          # Acces MongoDB pour Plant
│   │   ├── SpeciesRepository.java        # Acces MongoDB pour Species
│   │   ├── ForestRepository.java         # Acces MongoDB pour Forest
│   │   ├── SeasonCycleRepository.java    # Acces MongoDB pour SeasonCycle
│   │   ├── EffectRepository.java         # Acces MongoDB pour Effect
│   │   ├── PlantEffectRepository.java    # Acces MongoDB pour PlantEffect
│   │   └── SensorReadingRepository.java  # Acces MongoDB pour SensorReading
│   │
│   ├── services/
│   │   ├── PlantServices.java            # Logique metier des plantes
│   │   ├── SpeciesServices.java          # Logique metier des especes
│   │   ├── ForestService.java            # Logique metier des forets (R1/R2)
│   │   ├── SeasonService.java            # Logique metier des saisons
│   │   ├── EffectService.java            # Logique metier des effets
│   │   ├── EnvironmentServices.java      # Evolution de l'environnement
│   │   └── Simulation.java               # Simulation manuelle ou horaire
│   │
│   ├── GreenDesk.java                    # Classe principale Spring Boot
│   └── Main.java                         # Point d'entree alternatif
│
├── src/main/resources/
│   └── application.properties            # Configuration MongoDB & serveur
│
├── src/test/java/org/example/
│   ├── TestPlantServices.java            # Tests des services plantes
│   ├── TestSpeciesServices.java          # Tests des services especes
│   ├── TestSimulationEnvironment.java    # Tests de simulation
│   ├── TestForestAndSeasons.java         # Tests L2-F1 (8 tests)
│   └── TestEffects.java                  # Tests L2-F2 (5 tests)
│
├── Dockerfile                            # Multi-stage build pour conteneur app
├── docker-compose.yml                    # Orchestration 3 services
├── .dockerignore                         # Optimisation build Docker
├── DOCKER.md                             # Documentation Docker complete
├── test-docker.sh                        # Script de test automatise
├── build.gradle                          # Configuration Gradle
└── README.md                             # Ce fichier
```

## Installation et lancement

### Prerequis

- Java 21+
- Gradle 9.2.0+
- MongoDB Atlas ou MongoDB local

### Option 1: Lancement local (sans Docker)

1. **Cloner le depot**
```bash
git clone <repo-url>
cd GreenDesk
```

2. **Configurer MongoDB**

Editer `src/main/resources/application.properties`:

```properties
# MongoDB Atlas
spring.data.mongodb.uri=mongodb+srv://USER:PASSWORD@cluster.mongodb.net/
spring.data.mongodb.database=greendesk

# OU MongoDB local
spring.data.mongodb.uri=mongodb://localhost:27017/greendesk

server.port=8080
```

3. **Lancer l'application**
```bash
./gradlew bootRun
```

L'API demarre sur http://localhost:8080

### Option 2: Lancement avec Docker (recommande)

1. **Prerequis Docker**
- Docker Engine installe
- Docker Compose installe

2. **Lancer tous les services**
```bash
docker compose up -d
```

Cette commande lance :
- **app**: Application Spring Boot (port 8080)
- **mongodb**: Base de donnees MongoDB (port 27017)
- **mongo-express**: Interface web MongoDB (port 8081)

3. **Verifier les services**
```bash
# Statut des conteneurs
docker compose ps

# Logs de l'application
docker compose logs -f app

# Script de test automatise
./test-docker.sh
```

4. **Acces aux services**
- Application API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/v3/api-docs
- Mongo Express: http://localhost:8081 (admin/admin)
- MongoDB: mongodb://localhost:27017

5. **Arreter les services**
```bash
# Arreter les conteneurs
docker compose down

# Arreter et supprimer les donnees
docker compose down -v
```

Voir [DOCKER.md](DOCKER.md) pour la documentation Docker complete.

## Utilisation de l'API

### Documentation interactive

Acceder a Swagger UI pour une documentation interactive complete:
- URL: http://localhost:8080/swagger-ui.html
- Tous les endpoints y sont documentes avec possibilite de test direct

### Exemples d'utilisation

#### Gestion des especes

**Creer une espece**
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

**Lister toutes les especes**
```bash
curl http://localhost:8080/api/species
```

**Recuperer une espece par nom**
```bash
curl http://localhost:8080/api/species/Tomato
```

**Mettre a jour une espece**
```bash
curl -X PUT http://localhost:8080/api/species/SPECIES_ID \
-H "Content-Type: application/json" \
-d '{"optimalWaterNeeds": 250}'
```

**Supprimer une espece**
```bash
curl -X DELETE http://localhost:8080/api/species/SPECIES_ID
```

#### Gestion des plantes

**Creer une plante**
```bash
curl -X POST "http://localhost:8080/plants/create?name=Tomato_Plant_1&speciesId=SPECIES_ID"
```

**Lister toutes les plantes**
```bash
curl http://localhost:8080/plants
```

**Recuperer une plante par ID**
```bash
curl http://localhost:8080/plants/PLANT_ID
```

**Consulter l'etat d'une plante**
```bash
curl http://localhost:8080/plants/PLANT_ID/state
```

Reponse possible: `HEALTHY`, `STRESSED`, `DORMANT`, `DISEASED`

**Mettre a jour une plante**
```bash
curl -X PUT "http://localhost:8080/plants/PLANT_ID?water=220&temperature=23"
```

**Supprimer une plante**
```bash
curl -X DELETE http://localhost:8080/plants/PLANT_ID
```

#### Gestion des forets

**Creer une foret**
```bash
curl -X POST http://localhost:8080/api/forests \
-H "Content-Type: application/json" \
-d '{
  "name": "Forest1",
  "width": 10,
  "height": 10
}'
```

**Ajouter une plante a une foret**
```bash
curl -X POST "http://localhost:8080/api/forests/FOREST_ID/plants/PLANT_ID?x=3&y=5"
```

**Lister les plantes d'une foret**
```bash
curl http://localhost:8080/api/forests/FOREST_ID/plants
```

**Supprimer une foret**
```bash
curl -X DELETE http://localhost:8080/api/forests/FOREST_ID
```

#### Gestion des saisons

**Lister toutes les saisons disponibles**
```bash
curl http://localhost:8080/api/seasons
```

**Recuperer une saison par type**
```bash
curl http://localhost:8080/api/seasons/WINTER
```

**Obtenir la saison actuelle d'une foret**
```bash
curl http://localhost:8080/api/forests/FOREST_ID/current-season
```

**Faire progresser la saison d'une foret**
```bash
curl -X POST http://localhost:8080/api/forests/FOREST_ID/advance-season
```

#### Gestion des effets

**Lister tous les effets disponibles**
```bash
curl http://localhost:8080/api/effects
```

**Recuperer un effet par nom**
```bash
curl http://localhost:8080/api/effects/Shade
```

**Appliquer un effet a une plante**
```bash
curl -X POST http://localhost:8080/api/plants/PLANT_ID/effects/EFFECT_ID
```

**Lister les effets actifs d'une plante**
```bash
curl http://localhost:8080/api/plants/PLANT_ID/effects
```

**Retirer un effet d'une plante**
```bash
curl -X DELETE http://localhost:8080/api/plants/PLANT_ID/effects/EFFECT_ID
```

## Tests

Le projet inclut une suite de tests complete:

### Execution des tests

```bash
# Tous les tests
./gradlew test

# Tests specifiques
./gradlew test --tests TestForestAndSeasons
./gradlew test --tests TestEffects
```

### Suite de tests

- **TestPlantServices.java**: Tests des services de gestion des plantes
- **TestSpeciesServices.java**: Tests des services de gestion des especes
- **TestSimulationEnvironment.java**: Tests de la simulation environnementale
- **TestForestAndSeasons.java**: 8 tests pour L2-F1
  - Creation de foret
  - Ajout de plantes avec validation R1 (unicite position)
  - Diversite genetique R2 (variationSeed)
  - Gestion des saisons et cycles
- **TestEffects.java**: 5 tests pour L2-F2
  - Application d'effets
  - Calcul des modificateurs
  - Integration dans l'evolution des plantes

Total: 13 tests

### Resultats attendus

Tous les tests doivent passer:
```
BUILD SUCCESSFUL
```

## Architecture et fonctionnement

### Modeles principaux

- **Species**: Definit les besoins ideaux d'une plante et ses taux de croissance/production
- **Plant**: Liee a une espece, possede ses valeurs actuelles et son stressIndex
- **Forest**: Contient des plantes avec leurs positions (x, y)
- **Season**: Modificateurs environnementaux selon la saison
- **SeasonCycle**: Gestion du cycle des saisons pour une foret
- **Effect**: Modificateurs appliques a une plante
- **PlantEffect**: Association entre une plante et un effet actif
- **PlantState**: Enum representant l'etat calcule dynamiquement selon l'ecart aux besoins optimaux

### Services

- **SpeciesServices**: Gestion CRUD des especes
- **PlantServices**: Gestion des plantes + calcul d'etat + evolution (avec effets)
- **ForestService**: Gestion des forets + validation R1/R2
- **SeasonService**: Catalogue des saisons + gestion des cycles
- **EffectService**: Catalogue des effets + calcul des modificateurs totaux
- **EnvironmentServices**: Evolution de l'environnement des plantes
- **Simulation**: Simulation automatique ou manuelle

### Controllers

- **HomeController**: Redirection de / vers Swagger UI
- **SpeciesController**: Endpoints REST pour les especes
- **PlantController**: Endpoints REST pour les plantes
- **ForestController**: Endpoints REST pour les forets
- **SeasonController**: Endpoints REST pour les saisons
- **ForestSeasonController**: Gestion des saisons des forets
- **EffectController**: Endpoints REST pour les effets

### Repositories

- Tous heritent de `MongoRepository` pour l'acces a MongoDB
- Methodes de recherche personnalisees selon les besoins

## Regles metier implementees

### R1: Unicite des positions dans une foret
- Une position (x, y) ne peut etre occupee que par une seule plante
- HTTP 409 Conflict si position deja occupee

### R2: Diversite genetique
- Chaque plante recoit un `variationSeed` unique
- Permet de differencier des plantes de meme espece
- Utilise pour creer de la diversite dans l'evolution

### Calcul de l'etat des plantes
1. Comparaison valeurs actuelles vs besoins optimaux de l'espece
2. Application des modificateurs de saison (si foret)
3. Application des modificateurs d'effets actifs
4. Calcul du stress index
5. Determination de l'etat: HEALTHY, STRESSED, DORMANT, DISEASED

### Evolution des plantes
- Prise en compte des effets actifs
- Modification de la croissance selon les modificateurs
- Impact sur l'etat de sante

## Documentation Docker

Voir [DOCKER.md](DOCKER.md) pour:
- Architecture Docker complete
- Configuration des services
- Commandes utiles
- Gestion des volumes et reseaux
- Depannage

## Roadmap

# Livraison 3 (L3) - Features L3-F1 & L3-F2
## I. FEATURE L3-F1 : GESTION DES EFFETS PERSONNALISÉS

L'objectif de cette fonctionnalité est d'offrir à l'utilisateur une flexibilité totale dans le soin apporté à ses plantes en lui permettant de créer ses propres "recettes" d'effets.

**Création d'Effets Custom :** Implémentation d'un système permettant de définir des modificateurs de température, d'eau et de stress index uniques.

**Persistance Différenciée :** Utilisation d'un attribut isCustom (boolean) pour séparer les effets natifs du simulateur des créations de l'utilisateur.

**Filtrage API :** Mise à jour des endpoints de consultation pour permettre l'affichage exclusif des effets personnalisés dans le dashboard utilisateur.


## II. FEATURE L3-F2 : STIMULUS, CLONAGE ET COMPARAISON

Cette fonctionnalité constitue l'outil d'analyse environnementale du projet. Elle permet de simuler des scénarios climatiques et de comparer les réactions physiologiques des plantes.

### 1. Stimulus de Masse par Forêt
Au lieu de cibler chaque plante individuellement, le système peut désormais appliquer un événement (ex: HEATWAVE, RAIN) à l'ensemble des plantes rattachées à un forestId spécifique.

**Logique :** Le stimulus génère automatiquement un effet temporaire appliqué à chaque membre de la forêt ciblée.

**Lancer une canicule sur une forêt :**

```Bash
#curl -X POST http://localhost:8080/api/stimuli \
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

**Réplication Totale :** Copie exacte des attributs (taille, niveaux, état).

**Génétique (Seed) :** Le variationSeed est également copié, assurant que l'originale et le clone réagissent de manière identique si les conditions sont égales.

**Cloner une plante (Témoin) :**

```Bash
curl -X POST "http://localhost:8080/api/plants/ID_PLANTE/clone?targetForestId=ID_FORET_BETA&x=2&y=2"
```
### 3. Rapport d'État Détaillé (/status)
Un nouvel endpoint de diagnostic affiche le Stress Index calculé, les données en temps réel des Capteurs (Eau, Température, Humidité, Lux) et le contexte environnemental.

**Consulter le diagnostic :**

```Bash
curl http://localhost:8080/api/plants/ID_PLANTE/status
```
## III. ARCHITECTURE TECHNIQUE ET API

Voici les points d'entrée (endpoints) ajoutés pour cette livraison :

**POST :** `/api/effects`

**Description :** Enregistre un nouvel effet personnalisé créé par l'utilisateur.

**POST :** `/api/stimuli`

**Description :** Déclenche un événement climatique (ex: canicule ou pluie) sur une forêt entière.

**POST :** `/api/plants/{id}/clone`

**Description :** Duplique une plante spécifique vers une forêt de test pour servir de témoin.

**GET :** `/plants/{id}/status`

**Description :** Affiche le rapport de diagnostic complet (stress, capteurs, état de santé).

## IV. PROTOCOLE DE VALIDATION (SCÉNARIO)

**Préparation :** Créer une plante dans la Forêt Alpha.

**Clonage :** Dupliquer cette plante dans la Forêt Beta.

**Simulation :** Envoyer un stimulus HEATWAVE sur la Forêt Alpha.

**Comparaison :** Consulter le /status des deux plantes. La plante "Alpha" doit présenter un stress thermique élevé, validant ainsi l'impact localisé du stimulus et l'efficacité du système de comparaison.

## Contribution

Pour contribuer au projet:
1. Fork le projet
2. Creer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## Licence

Ce projet est un projet academique developpe dans le cadre d'une formation.

## Contact

Pour toute question ou suggestion, veuillez ouvrir une issue sur le depot GitHub.

---



**Derniere mise a jour**: Janvier 2026
**Status**: Production ready avec Docker
