# Système d'effets

Les effets modifient les conditions environnementales des plantes pour simuler des interventions humaines ou des conditions naturelles.

```mermaid
flowchart LR
  A[GET /api/plants/{plantId}/status] --> B{Analyse état}
  B -->|STRESSED| C[POST /api/plants/{id}/effects/FERTILIZER]
  B -->|DRY| D[POST /api/plants/{id}/effects/EXTRA_WATERING]
  B -->|TOO_LIGHT| E[POST /api/plants/{id}/effects/SHADE]
  C --> F[DB: ajouter effect]
  D --> F
  E --> F
  F --> G[Service recalcul santé]
  G --> H[GET /api/plants/{plantId}/status (updated)]
  style A fill:#e0f7fa
  style G fill:#fff9c4
```

## Effets disponibles

### 1. SHADE (Ombrage)

**Impact** : Réduit la lumière de 30%

**Utilisation** :
- Protéger des plantes du soleil excessif
- Réduire le stress lumineux
- Idéal pour plantes d'ombre

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/SHADE
```

**Calcul** :
```
Avant : luxIntensity = 3000 lux
Après : luxIntensity = 3000 × 0.7 = 2100 lux
```

**Cas d'usage** :
- Plante stressée par lumière directe
- Humidité trop basse (ombre + arrosage)
- Été trop intense

---

### 2. FERTILIZER (Engrais)

**Impact** : Augmente la croissance de 20%

**Utilisation** :
- Accélérer la croissance
- Renforcer une plante affaiblie
- Augmenter productivité

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/FERTILIZER
```

**Calcul** :
```
Avant : baseGrowthRate = 2.5 %/jour
Après : baseGrowthRate = 2.5 × 1.2 = 3.0 %/jour
```

**Cas d'usage** :
- Plante qui grandit trop lentement
- Avant floraison
- Récupération après stress

---

### 3. EXTRA_WATERING (Arrosage supplémentaire)

**Impact** : Augmente l'eau de 15%

**Utilisation** :
- Compenser manque d'eau
- Réduire stress hydrique
- Augmenter humidité

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/EXTRA_WATERING
```

**Calcul** :
```
Avant : water = 400 ml
Après : water = 400 × 1.15 = 460 ml
```

**Cas d'usage** :
- Été (SUMMER) avec peu d'eau
- Plante stressée (STRESSED/DISEASED)
- Esp. qui aime l'eau (Fougère, Riz)

---

### 4. HEATING (Chauffage)

**Impact** : Augmente la température de 3°C

**Utilisation** :
- Réchauffer en hiver
- Aider plantes tropicales
- Réduire froid excessif

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/HEATING
```

**Calcul** :
```
Avant : temperature = 15°C
Après : temperature = 15 + 3 = 18°C
```

**Cas d'usage** :
- Hiver (WINTER) trop froid
- Plantes tropicales stressées
- Protection contre gel

---

## Gestion des effets

### Ajouter un effet

```bash
POST /api/plants/{plantId}/effects/{effectName}
```

**Effets** : SHADE, FERTILIZER, EXTRA_WATERING, HEATING

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/SHADE
```

### Lister les effets

```bash
GET /api/plants/{plantId}/effects
```

**Exemple** :
```bash
curl http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects
```

**Réponse** :
```json
["SHADE", "FERTILIZER"]
```

### Retirer un effet

```bash
DELETE /api/plants/{plantId}/effects/{effectName}
```

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/SHADE
```

## Combinaisons d'effets

Les effets s'accumulent ! Vous pouvez combiner plusieurs effets.

### Combinaisons recommandées

#### Croissance rapide
```bash
# Ajouter FERTILIZER + EXTRA_WATERING + HEATING
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/FERTILIZER
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/EXTRA_WATERING
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/HEATING

# Résultat :
# - Croissance +20%
# - Eau +15%
# - Température +3°C
# = Plante qui pousse très vite
```

#### Plante tropicale en hiver
```bash
# Ajouter HEATING + EXTRA_WATERING + SHADE
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/HEATING
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/EXTRA_WATERING
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/SHADE

# Résultat :
# - Température +3°C (chaud)
# - Eau +15% (humide)
# - Lumière -30% (ombre ≈ demande)
# = Conditions tropicales simulées
```

#### Plante d'ombre en récupération
```bash
# Ajouter SHADE + FERTILIZER + EXTRA_WATERING
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/SHADE
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/FERTILIZER
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/EXTRA_WATERING

# Résultat :
# - Ombrage (moins de stress)
# - Croissance +20% (récupération)
# - Eau +15% (bien hydratée)
```

### Combinaisons à éviter

```bash
# ❌ HEATING + SHADE uniquement
# Peut être confus écologiquement

# ❌ FERTILIZER seul sans eau
# Croissance rapide mais dehydratation

# ❌ EXTRA_WATERING en hiver (winter) sans vérif
# Risque de pourriture racines
```

## Scénarios d'application

### Scénario 1 : Plante stressée à secourir

**Situation** :
```
Status : STRESSED
Health : 65%
Raison : Température trop basse (5°C vs optimal 20°C)
```

**Plan d'action** :
```bash
# 1. Diagnostiquer
curl http://localhost:8080/api/plants/{plantId}

# 2. Ajouter HEATING immédiatement
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/HEATING

# 3. Vérifier eau (souvent connexe au froid)
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/EXTRA_WATERING

# 4. Ajouter FERTILIZER pour rétablissement
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/FERTILIZER

# 5. Monitorer 1-2 jours
curl http://localhost:8080/api/plants/{plantId}

# 6. Retirer progressivement quand health > 80%
curl -X DELETE http://localhost:8080/api/plants/{plantId}/effects/HEATING
```

**Résultat attendu** : Health revient > 80% → HEALTHY ✅

---

### Scénario 2 : Forêt en transition estivale

**Situation** :
```
Saison actuelle : SPRING
Effet : SUMMER arrive (température +5°C, eau ×1.2, lumière ×1.3)
```

**Préparation** :
```bash
# 1. Identifier plantes sensibles au soleil/chaleur
curl http://localhost:8080/api/forests/{forestId}/plants

# 2. Appliquer SHADE aux vulnérables
curl -X POST http://localhost:8080/api/plants/fougere_id/effects/SHADE

# 3. Appliquer HEATING aux tropicales (moins d'effet été = bonus)
curl -X POST http://localhost:8080/api/plants/orchidee_id/effects/HEATING

# 4. Avancer saison
curl -X POST http://localhost:8080/api/forests/{forestId}/season/next

# 5. Vérifier santé post-été
curl http://localhost:8080/api/forests/{forestId}/plants
```

---

### Scénario 3 : Optimiser forêt pour production

**Objectif** : Maximiser croissance et récolte de graines

**Stratégie** :
```bash
# Pour chaque plante productive :

# 1. Ajouter FERTILIZER (croissance +20%)
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/FERTILIZER

# 2. Ajouter EXTRA_WATERING (santé optimale)
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/EXTRA_WATERING

# 3. Optionnel : HEATING si saison froide
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/HEATING

# Résultat : Production maximale !
```

## Monitorage des effets

### Vérifier tous les effets d'une forêt
```bash
curl http://localhost:8080/api/forests/{forestId}/plants | \
  jq '.[] | {name, effects}'

# Résultat :
# {
#   "name": "Rose 1",
#   "effects": ["FERTILIZER", "EXTRA_WATERING"]
# }
```

### Comparer avec/sans effets
```bash
# Sans effets:
curl http://localhost:8080/api/plants/{plantId}
{
  "health": 70.0,
  "growth": 2.1
}

# Avec FERTILIZER:
curl -X POST http://localhost:8080/api/plants/{plantId}/effects/FERTILIZER

curl http://localhost:8080/api/plants/{plantId}
{
  "health": 70.0,
  "growth": 2.5   # +20%
}
```

## Questions fréquentes

**Q: Puis-je combiner tous les effets sur une plante ?**

A: Oui ! Tous les effets se cumulent. Mais ça peut être excessif.

**Q: Les effets affectent-ils la sanctité de la plante ?**

A: Non directement. Ils améliorent l'environnement qui affecte la santé.

**Q: Combien de temps durent les effets ?**

A: Jusqu'à suppression manuelle. Persistants jusqu'à `DELETE`.

**Q: Quels effets sont meilleurs ?**

A: Contexte-dépendant. Diagnostiquez d'abord le problème !

**Q: Puis-je appliquer le même effet 2 fois ?**

A: Non. Chaque effet unique par plante.

---

**Maîtrisé les effets ?** En savoir plus sur l'[Architecture](../architecture.md) !
