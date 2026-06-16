# Optimiseur de placement des plantes (Plant Placement Optimizer)

## Démarrage rapide

```bash
# 1) Démarrer l'application
./gradlew bootRun

# 2) Lancer le script de démonstration
./scripts/demo-placement-optimizer.bat  # Windows
./scripts/demo-placement-optimizer.sh   # Linux/Mac

# 3) Ouvrir dans le navigateur
http://localhost:8080/placement-optimizer.html
```

---

## Ce que fait la fonctionnalité

L’**Optimiseur de placement des plantes** utilise une approche d’IA basée sur des **algorithmes génétiques** pour trouver automatiquement de bonnes positions pour vos plantes dans une forêt (grille), en tenant compte notamment de :

1. ** Plantes compagnes (Companion planting)** : certaines espèces se renforcent (ex. Tomate + Basilic)
2. ** Compétition de ressources** : trop proches, les plantes se disputent eau et lumière
3. ** Prévention des maladies** : on espace les plantes d’une même espèce pour limiter la propagation
4. ** Qualité de position** : certaines zones (ex. plus centrales) peuvent être plus favorables

---

## Algorithme génétique : comment ça marche et pourquoi ici ?

### Fonctionnement général (en 6 étapes)
Un **algorithme génétique** est une méthode d’optimisation inspirée de l’évolution :
1. **Population initiale** : on génère plusieurs solutions candidates (placements)  
2. **Évaluation (fitness)** : on attribue un score à chaque solution selon des critères métier  
3. **Sélection** : on garde surtout les meilleures solutions comme “parents”  
4. **Croisement (crossover)** : on combine deux solutions pour produire de nouvelles solutions  
5. **Mutation** : on applique de petites modifications aléatoires (déplacer/échanger une plante)  
6. **Itérations** : on répète sur plusieurs “générations” et on conserve la meilleure solution

### Pourquoi l’appliquer au placement des plantes ?
Le placement sur une grille est un problème **combinatoire** : dès qu’il y a plusieurs plantes et plusieurs cases, le nombre de configurations possibles explose. Tester toutes les options est trop coûteux, et un placement manuel/aléatoire est souvent sous-optimal.  
L’algorithme génétique permet de trouver **rapidement** une disposition globalement meilleure en optimisant plusieurs critères à la fois (compatibilités, distances, compétition, prévention des maladies).

---

## Fonctionnalités

### 🧬 Optimisation par algorithme génétique
- **Recherche par population** : teste 100 arrangements en parallèle
- **Amélioration itérative** : combine les meilleures solutions sur 200 générations
- **Mutations “intelligentes”** : explore de nouveaux arrangements
- **Élitisme** : conserve toujours les 5 meilleures solutions

###  Visualisation Heatmap
- Carte colorée des zones optimales de placement
- Zones vertes = meilleur placement
- Zones rouges = placement défavorable
- Mise à jour selon les plantes déjà présentes

###  Interface interactive
- Grille visuelle de la forêt (interface drag-and-drop)
- Affichage en direct du score de fitness
- Marqueurs de plantes par espèce
- Application du résultat en un clic

###  Suggestions
- Propose la **meilleure position unique** pour une espèce
- Basée sur l’état actuel de la forêt
- Prend en compte l’ensemble des critères d’optimisation

---

## Référence API

### 1) Optimiser le placement (prévisualisation)

**Endpoint** : `POST /api/placement/optimize`

**Requête** :
```json
{
  "plantIds": ["plant1_id", "plant2_id", "plant3_id"],
  "forestWidth": 10,
  "forestHeight": 10
}
```

**Réponse** :
```json
{
  "fitnessScore": 15.67,
  "placements": [
    {"plantId": "plant1_id", "speciesName": "Tomato", "x": 3, "y": 4},
    {"plantId": "plant2_id", "speciesName": "Basil", "x": 4, "y": 4},
    {"plantId": "plant3_id", "speciesName": "Carrot", "x": 5, "y": 6}
  ],
  "message": "Optimization completed successfully"
}
```

### 2) Optimiser et appliquer (enregistrer)

**Endpoint** : `POST /api/placement/optimize-and-apply/{forestId}`

**Requête** : tableau d’identifiants de plantes
```json
["plant1_id", "plant2_id", "plant3_id"]
```

**Réponse** :
```json
{
  "forestId": "forest123",
  "fitnessScore": 15.67,
  "plantsPlaced": 3,
  "message": "Optimization applied to forest"
}
```

### 3) Générer une heatmap

**Endpoint** : `GET /api/placement/heatmap/{forestId}?species=Tomato`

**Réponse** :
```json
{
  "forestId": "forest123",
  "species": "Tomato",
  "width": 10,
  "height": 10,
  "heatmap": [
    {"x": 0, "y": 0, "score": 0.65, "recommendedSpecies": "Tomato"},
    {"x": 0, "y": 1, "score": 0.72, "recommendedSpecies": "Tomato"},
    {"x": 0, "y": 2, "score": 0.0, "recommendedSpecies": "Tomato"}
  ]
}
```

**Interprétation du score** :
- `1.0` = Placement optimal
- `0.5-0.9` = Bon placement
- `0.3-0.5` = Acceptable
- `0.0-0.3` = Mauvais placement
- `0.0` = Occupé ou inadapté

### 4) Obtenir une suggestion de position

**Endpoint** : `GET /api/placement/suggest/{forestId}?species=Basil`

**Réponse** :
```json
{
  "x": 5,
  "y": 7,
  "score": 0.89,
  "species": "Basil"
}
```

---

## Score de fitness (explication)

Le score de fitness indique à quel point un arrangement de placement est “bon”.

### Facteurs positifs (+)
- **Plantes compagnes proches** (distance ≤ 3 cases) : +0.1 à +0.8 par paire
- **Espacement optimal même espèce** (3–5 cases) : +1.0 par paire
- **Position centrale** : +0.1 par case éloignée du bord

### Facteurs négatifs (−)
- **Espèces incompatibles proches** : -0.3 à -0.7 par paire
- **Même espèce trop proche** (< 3 cases) : -2.0 par paire
- **Compétition ressources** (< 2 cases) : -0.5 par paire

### Exemples d’interprétation
- **Score 20+** : Excellent
- **Score 10–20** : Bon
- **Score 5–10** : Acceptable
- **Score < 5** : Faible
- **Score négatif** : Très mauvais

---

## Matrice de compatibilité des espèces

| Espèce A | Espèce B | Score | Raison |
|---------|----------|-------|--------|
| Tomato | Basil | +0.8 | Le basilic repousse certains parasites |
| Tomato | Carrot | +0.5 | La carotte aère le sol |
| Tomato | Potato | -0.7 | Maladies communes (mildiou) |
| Tomato | Tomato | -0.3 | Compétition de ressources |
| Basil | Pepper | +0.6 | Le basilic favorise la croissance |
| Carrot | Onion | +0.7 | L’oignon repousse la mouche de la carotte |

### Ajouter une compatibilité personnalisée

Modifier `PlacementOptimizationService.java` :

```java
Map<String, Double> customCompat = new HashMap<>();
customCompat.put("SpeciesB", 0.9);  // Très bénéfique
customCompat.put("SpeciesC", -0.5); // Légèrement nuisible
matrix.put("SpeciesA", customCompat);
```

---

## Paramètres de l’algorithme

### Valeurs par défaut
```java
POPULATION_SIZE = 100      // Nombre de solutions par génération
MAX_GENERATIONS = 200      // Nombre d’itérations
MUTATION_RATE = 0.15       // 15% de chance de mutation
CROSSOVER_RATE = 0.7       // 70% de chance de croisement
ELITE_COUNT = 5            // Top solutions conservées
```

### Ajustement performance

**Plus rapide (qualité moindre)** :
```java
POPULATION_SIZE = 50
MAX_GENERATIONS = 100
```

**Meilleurs résultats (plus lent)** :
```java
POPULATION_SIZE = 200
MAX_GENERATIONS = 500
```

**Plus d’exploration** :
```java
MUTATION_RATE = 0.25
CROSSOVER_RATE = 0.8
```

---

## Cas d’usage

### 1) Planifier une nouvelle forêt
```bash
# Créer une forêt vide
curl -X POST http://localhost:8080/api/forests -H "Content-Type: application/json"   -d '{"name":"New Garden","width":15,"height":15}'

# Créer des plantes (sans les placer)
curl -X POST http://localhost:8080/plants/create?name=Tomato1&speciesId=SPECIES_ID

# Optimiser et appliquer en une fois
POST /api/placement/optimize-and-apply/{forestId}
```

### 2) Étendre une forêt existante
```bash
# Obtenir une heatmap pour une nouvelle espèce
GET /api/placement/heatmap/{forestId}?species=Basil

# Trouver la meilleure position unique
GET /api/placement/suggest/{forestId}?species=Basil

# Placer manuellement ou optimiser en batch
```

### 3) Repenser l’agencement
```bash
# Retirer des plantes de la forêt (UI ou API)
DELETE /api/forests/{forestId}/plants/{plantId}

# Ré-optimiser les plantes restantes
POST /api/placement/optimize
```

### 4) Comparer plusieurs arrangements
```bash
# Lancer plusieurs fois (non déterministe)
# Comparer les fitness scores
# Garder le meilleur résultat
```

---

## Utilisation Frontend

### Parcours utilisateur (UI)

1. **Sélectionner une forêt**
   - Choisir une forêt existante
   - Ou créer une nouvelle forêt

2. **Sélectionner des plantes**
   - Cocher les plantes à optimiser
   - Mixer des espèces compatibles améliore le résultat

3. **Lancer l’optimisation**
   - Cliquer sur “🧬 Run Genetic Algorithm”
   - Attendre selon la complexité
   - Visualiser le fitness score

4. **Vérifier le résultat**
   - Les cases en pointillés indiquent les positions proposées
   - Les couleurs indiquent les espèces

5. **Appliquer ou relancer**
   - “✓ Apply to Forest” pour sauvegarder
   - Ou ajuster la sélection et relancer

6. **Utiliser la heatmap**
   - Choisir une espèce
   - Cliquer “🔥 Show Heatmap”
   - Vert = bon, Rouge = mauvais

### Raccourcis clavier
- `Ctrl+R` : rafraîchir le canvas
- `Ctrl+O` : lancer l’optimisation
- `Ctrl+A` : appliquer le résultat

---

## Tests

### Lancer tous les tests
```bash
./gradlew test --tests TestPlacementOptimization
```

### Couverture de test (exemples)
- ✅ Optimisation multi-espèces (10 plantes)
- ✅ Validation espacement même espèce
- ✅ Précision génération heatmap
- ✅ Cas limites (trop de plantes)
- ✅ Gestion liste vide
- ✅ Calcul du fitness score
- ✅ Validation des bornes (positions)

### Checklist de test manuel
- [ ] Créer forêt et plantes via l’UI
- [ ] Lancer optimisation avec espèces variées
- [ ] Vérifier absence de chevauchement
- [ ] Vérifier fitness score positif
- [ ] Générer heatmap par espèce
- [ ] Appliquer et vérifier la persistance après reload
- [ ] Tester avec 50+ plantes

---

## Dépannage

### Problème : score de fitness faible

**Causes** :
- Trop de plantes pour la taille de la forêt
- Espèces incompatibles
- Manque de données de compatibilité

**Solutions** :
1. Augmenter la taille de la forêt
2. Réduire le nombre de plantes
3. Ajouter des règles de compatibilité
4. Vérifier les noms d’espèces

### Problème : optimisation trop lente

**Causes** :
- Grande forêt
- Beaucoup de plantes
- Trop de générations

**Solutions** :
1. Diminuer `POPULATION_SIZE`
2. Diminuer `MAX_GENERATIONS`
3. Optimiser en plusieurs lots
4. Machine plus puissante

### Problème : heatmap à zéro

**Causes** :
- Forêt pleine
- Mauvais nom d’espèce
- Pas de plantes existantes

**Solutions** :
1. Libérer des cases
2. Vérifier le nom d’espèce
3. Ajouter au moins une plante

### Problème : “No valid plants found”

**Causes** :
- IDs invalides
- Plantes supprimées
- Mauvaise forêt

**Solutions** :
1. Vérifier `GET /api/plants`
2. Vérifier les IDs envoyés
3. Vérifier la forêt sélectionnée

---

## Bonnes pratiques

### 🌟 Planification
- Démarrer avec de petites forêts (10x10)
- Mixer des espèces compatibles
- Garder ~20% d’espace libre

### 🎯 Optimisation
- Lancer plusieurs fois et garder le meilleur
- Pour grandes forêts : optimiser par sections
- Ajuster paramètres vitesse/qualité

### 📊 Suivi
- Comparer les fitness scores dans le temps
- Documenter les arrangements réussis

### 🔄 Maintenance
- Ré-optimiser lors de l’ajout d’espèces
- Mettre à jour la matrice de compatibilité

---

## Métriques de performance

### Temps d’exécution (ordre d’idée)
- **10 plantes, forêt 10x10** : ~2–3 s
- **20 plantes, forêt 15x15** : ~5–8 s
- **50 plantes, forêt 20x20** : ~15–25 s
- **100 plantes, forêt 30x30** : ~60–120 s

### Scalabilité (simplifié)
- **Complexité temps** : O(P² × G × N)  
  P = taille population, G = générations, N = nombre de plantes  
- **Complexité mémoire** : O(P × N)

---

## Améliorations futures

### Court terme
- [ ] Drag-and-drop avec fitness en direct
- [ ] Historique undo/redo
- [ ] Export CSV/PDF
- [ ] Sauvegarde/chargement de matrices personnalisées

### Moyen terme
- [ ] Optimisation multi-objectifs (rendement + santé + esthétique)
- [ ] Prise en compte soleil/ombre saisonnier
- [ ] Prédiction selon stade de croissance
- [ ] Regroupement par zones d’irrigation

### Long terme
- [ ] Visualisation 3D
- [ ] Apprentissage à partir de résultats réels
- [ ] Intégration capteurs IoT
- [ ] Application mobile

---

## Références académiques

1. **Holland, J. H.** (1992). *Adaptation in Natural and Artificial Systems*. MIT Press.
2. **Riotte, L.** (1998). *Carrots Love Tomatoes: Secrets of Companion Planting*. Storey Publishing.
3. **Michalewicz, Z.** (1996). *Genetic Algorithms + Data Structures = Evolution Programs*. Springer.

---

## Licence

Cette fonctionnalité fait partie de **GreenDesk**, projet académique.

---

## Support

- **Documentation** : `/docs/PLACEMENT_OPTIMIZER.md`
- **Script de démo** : `scripts/demo-placement-optimizer.bat`
- **Tests** : `src/test/java/org/example/TestPlacementOptimization.java`

---

**Bonnes optimisations ! **
