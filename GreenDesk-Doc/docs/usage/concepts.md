# Concepts fondamentaux

Comprendre les concepts clés de GreenDesk est essentiel pour l'utiliser efficacement.

## Architecture conceptuelle

```
┌─────────────────────────────────────────────┐
│           Forêt (Écosystème)                │
│  ┌──────────────────────────────────────┐   │
│  │      Grille spatiale (10x10)         │   │
│  │  ┌────────┬────────┬────────────┐    │   │
│  │  │ Plante │        │ Plante     │    │   │
│  │  │ (Rose) │ Vide   │ (Chêne)    │    │   │
│  │  └────────┴────────┴────────────┘    │   │
│  │  Saison actuelle : SPRING             │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
         │
         └──> Plantes (Plants)
              │
              ├──> Espèce (Species)
              ├──> État (Status)
              ├──> Environnement
              └──> Effets (Effects)
```

## Entités principales

### 1. Espèce (Species)

Une **espèce** définit les caractéristiques d'un type de plante.

**Attributs** :

```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "Rose",
  "waterNeeds": 500.0,           // Besoin optimal en eau (ml)
  "optimalTemperature": 20.0,    // Température optimale (°C)
  "optimalHumidity": 60.0,       // Humidité optimale (%)
  "luxNeeds": 3000.0,            // Besoin en lumière (lux)
  "baseGrowthRate": 2.5,         // Taux de croissance de base (%)
  "seedProductionRate": 50.0     // Taux de production de graines
}
```

**Rôle** : Les espèces sont des modèles réutilisables. Une Rose créée une fois peut servir de base à plusieurs plantes.

### 2. Plante (Plant)

Une **plante** est une instance d'une espèce particulière.

**Attributs** :

```json
{
  "id": "507f1f77bcf86cd799439012",
  "name": "Ma Rose",
  "species": "Rose",
  "status": "HEALTHY",           // État de la plante
  "age": 15,                     // Jours
  "health": 95.0,                // Santé (%)
  "growth": 8.5,                 // Croissance (mm)
  "environment": {
    "water": 450.0,              // Eau actuelle (ml)
    "temperature": 22.0,         // Température (°C)
    "humidity": 58.0,            // Humidité (%)
    "luxIntensity": 2900.0       // Lumière (lux)
  },
  "effects": ["FERTILIZER"]      // Effets actifs
}
```

### 3. Forêt (Forest)

Une **forêt** est un écosystème 2D avec une grille spatiale.

**Attributs** :

```json
{
  "id": "507f1f77bcf86cd799439013",
  "name": "Forêt Enchantée",
  "width": 10,
  "height": 10,
  "currentSeason": "SPRING",
  "plants": [
    {
      "plantId": "507f1f77bcf86cd799439012",
      "posX": 5,
      "posY": 5,
      "variationSeed": 0.95       // Variation génétique
    }
  ]
}
```

**Caractéristiques** :

- Une position = une seule plante (unicité)
- Variation génétique via `variationSeed`
- Cycle de saisons automatique
- Grille configurable (WxH)

### 4. Saison (Season)

Les saisons modifient l'environnement des plantes.

**Saisons prédéfinies** :

```json
{
  "WINTER": {
    "waterMultiplier": 0.5,
    "temperatureModifier": -5.0,
    "humidityModifier": 10.0,
    "luxMultiplier": 0.7
  },
  "SPRING": {
    "waterMultiplier": 1.0,
    "temperatureModifier": 0.0,
    "humidityModifier": 5.0,
    "luxMultiplier": 1.0
  },
  "SUMMER": {
    "waterMultiplier": 1.2,
    "temperatureModifier": 5.0,
    "humidityModifier": -10.0,
    "luxMultiplier": 1.3
  },
  "AUTUMN": {
    "waterMultiplier": 0.8,
    "temperatureModifier": -2.0,
    "humidityModifier": 0.0,
    "luxMultiplier": 0.9
  }
}
```

### 5. Effet (Effect)

Les effets modifient les conditions environnementales d'une plante.

**Effets predefinis** :

| Effet | Impact |
|-------|--------|
| **SHADE** | Lumière -30% |
| **FERTILIZER** | Croissance +20% |
| **EXTRA_WATERING** | Eau +15% |
| **HEATING** | Température +3°C |

**Exemple** :

```json
{
  "plantId": "507f1f77bcf86cd799439012",
  "effects": ["SHADE", "FERTILIZER"],
  "calculatedEnvironment": {
    "water": 517.5,              // 15% +
    "luxIntensity": 2030.0       // 30% -
  }
}
```

## États d'une plante

L'état d'une plante (`status`) est calculé automatiquement :

```mermaid
stateDiagram-v2
  ---
  # Concepts fondamentaux de GreenDesk

  Cette page présente les concepts clés pour comprendre et exploiter toute la puissance de GreenDesk.

  ## Vue d'ensemble conceptuelle

  GreenDesk modélise un écosystème végétal complet : espèces, plantes, forêts, saisons, effets, environnement.

  ### Schéma conceptuel

  ```mermaid
  flowchart TD
      Forest["Forêt (Écosystème 2D)"]
      Grid["Grille spatiale (10x10)"]
      Plant1["Plante (Rose)"]
      Plant2["Plante (Chêne)"]
      Empty["Vide"]
      Season["Saison actuelle : SPRING"]
      Forest --> Grid
      Grid --> Plant1
      Grid --> Empty
      Grid --> Plant2
      Grid --> Season
      Plant1 --> Species
      Plant1 --> Status
      Plant1 --> Environment
      Plant1 --> Effects
      Species["Espèce"]
      Status["État"]
      Environment["Environnement"]
      Effects["Effets"]
  ```

  ## Entités principales

  ### Espèce (Species)
  Définit les caractéristiques génétiques et besoins optimaux d'un type de plante.

  **Exemple d'attributs** :
  ```json
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "Rose",
    "waterNeeds": 500.0,
    "optimalTemperature": 20.0,
    "optimalHumidity": 60.0,
    "luxNeeds": 3000.0,
    "baseGrowthRate": 2.5,
    "seedProductionRate": 50.0
  }
  ```
  **Rôle** : modèle réutilisable pour créer plusieurs plantes.

  ### Plante (Plant)
  Instance concrète d'une espèce, avec état, environnement et historique propres.

  **Exemple d'attributs** :
  ```json
  {
    "id": "507f1f77bcf86cd799439012",
    "name": "Ma Rose",
    "species": "Rose",
    "status": "HEALTHY",
    "age": 15,
    "health": 95.0,
    "growth": 8.5,
    "environment": {
      "water": 450.0,
      "temperature": 22.0,
      "humidity": 58.0,
      "luxIntensity": 2900.0
    },
    "effects": ["FERTILIZER"]
  }
  ```
  **Rôle** : représente une plante vivante, évolutive, soumise à des effets et à l'environnement.

  ### Forêt (Forest)
  Écosystème 2D, grille spatiale contenant plusieurs plantes, gère les saisons et interactions.

  **Exemple d'attributs** :
  ```json
  {
    "id": "507f1f77bcf86cd799439014",
    "name": "Forêt Enchantée",
    "width": 10,
    "height": 10,
    "currentSeason": "SPRING",
    "plants": [ ... ]
  }
  ```

  ### Effet (Effect)
  Action ponctuelle ou continue modifiant l'état d'une plante (ex : engrais, ombrage, arrosage).

  **Exemple** :
  ```json
  {
    "name": "FERTILIZER",
    "impact": 0.2,
    "description": "Augmente la croissance de 20%"
  }
  ```

  ### Environnement
  Conditions locales autour de la plante (eau, température, humidité, lumière).

  **Exemple** :
  ```json
  {
    "water": 450.0,
    "temperature": 22.0,
    "humidity": 58.0,
    "luxIntensity": 2900.0
  }
  ```

  ## Cycle de vie d'une plante

  1. Création d'une espèce (modèle génétique)
  2. Instanciation d'une plante à partir d'une espèce
  3. Placement dans une forêt (optionnel)
  4. Application d'effets (engrais, arrosage, ombrage...)
  5. Suivi de l'évolution (croissance, santé, stress)
  6. Passage des saisons, adaptation de l'environnement

  Pour des exemples concrets, consultez les pages suivantes :
  - [Gestion des espèces](species.md)
  - [Gestion des plantes](plants.md)
  - [Gestion des forêts](forests.md)
  - [Système d'effets](effects.md)
```
if Santé > 80    → HEALTHY
else if Santé > 50 → STRESSED
else if Santé > 20 → DORMANT
else             → DISEASED
```

## Exemple complet

### Créer une simulation

1. **Créer une espèce** : "Rose"
   - waterNeeds: 500ml
   - optimalTemperature: 20°C
   - optimalHumidity: 60%
   - luxNeeds: 3000 lux

2. **Créer une plante** : "Rose du Jardin"
   - water: 500ml (optimal)
   - temperature: 25°C (5°C trop chaud)
   - humidity: 60% (optimal)
   - lux: 3000 (optimal)

3. **Calculer la santé**

```
Stress_water     = 0 / 500 = 0.00
Stress_temp      = 5 / 20 = 0.25
Stress_humidity  = 0 / 60 = 0.00
Stress_lux       = 0 / 3000 = 0.00
StressTotal      = 0.25

Santé = 100 - (0.25 × 50) = 87.5%
→ État = HEALTHY
```

4. **Placer dans une forêt**
   - Position (5, 5) dans "Forêt Enchantée"
   - Variation génétique : 0.95

5. **Appliquer des effets**
   - HEATING : +3°C
   - Nouvelle température : 28°C
   - Recalcul du stress

## Concepts avancés

### Variation génétique

Chaque plante dans une forêt peut avoir une `variationSeed` (0.8 - 1.2) qui modifie ses paramètres d'une manière pseudo-aléatoire mais reproductible.

### Diversité écologique

Les forêts encouragent la diversité en :
- Limitant une espèce par position
- Appliquant des variations génétiques
- Permettant l'application sélective d'effets

### Simulation écologique

GreenDesk simule un écosystème simplifié mais réaliste :
- Saisons affectent conditions
- Effets modifient environnement
- État affecte croissance
- Plantes évoluent dynamiquement

---

Maintenant que vous comprenez les concepts, consultez les guides spécifiques pour [Espèces](species.md), [Plantes](plants.md), [Forêts](forests.md) et [Effets](effects.md) !
