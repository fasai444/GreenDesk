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

- ✅ Une position = une seule plante (unicité)
- ✅ Variation génétique via `variationSeed`
- ✅ Cycle de saisons automatique
- ✅ Grille configurable (WxH)

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
  [*] --> HEALTHY: Health > 80%
    
  HEALTHY --> STRESSED: Stress increases\nHealth 50-80%
  STRESSED --> HEALTHY: Conditions improve\nHealth > 80%
  STRESSED --> DORMANT: Severe stress\nHealth 20-50%
  DORMANT --> STRESSED: Partial recovery\nHealth 50-80%
  DORMANT --> DISEASED: Critical state\nHealth < 20%
  DISEASED --> DORMANT: Treatment applied\nHealth 20-50%
  STRESSED --> DISEASED: Critical stress\nHealth < 20%
  DISEASED --> [*]: Death
    
  note right of HEALTHY
    ✅ All conditions optimal
    or near optimal
  end note
    
  note right of STRESSED
    ⚠️ Some conditions
    suboptimal
  end note
    
  note right of DORMANT
    🟠 Growth stopped
    severe conditions
  end note
    
  note right of DISEASED
    🔴 Critical state
    urgent intervention
  end note
```

**États possibles** :

| État | Santé | Description |
|------|-------|-------------|
| **HEALTHY** | > 80% | Tout va bien |
| **STRESSED** | 50-80% | Conditions suboptimales |
| **DORMANT** | 20-50% | Croissance arrêtée |
| **DISEASED** | < 20% | Grave problème |

## Cycle de vie d'une plante

```
Création
   ↓
   ├─ Initialisation avec paramètres par défaut
   ├─ Liée à une espèce
   ├─ Peut être placée dans une forêt
   ↓
Évolution
   ├─ Quotidienne (simulation)
   ├─ Affectée par saisons/effets
   ├─ Mise à jour santé/croissance/age
   ↓
Application d'effets
   ├─ Modification environnement
   ├─ Impact sur la santé
   ↓
Fin de vie
   └─ Suppression
```

## Formules de calcul

### Santé

```
Stress = Σ(|ValeurActuelle - ValeurOptimale| / ValeurOptimale)
Santé = max(0, 100 - (Stress * 50))
```

### Croissance (mm/jour)

```
Croissance = BaseGrowthRate × (Santé/100) × FacteurSaison × FacteurEffets
```

### État

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
→ État = HEALTHY ✅
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
