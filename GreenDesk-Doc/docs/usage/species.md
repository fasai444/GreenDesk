# Gestion des espèces

Les espèces définissent les caractéristiques de base des plantes.

## Créer une espèce

### Attributs requis

```json
{
  "name": "Rose",                    // Nom unique de l'espèce
  "waterNeeds": 500.0,               // Besoin optimal en eau (ml/jour)
  "optimalTemperature": 20.0,        // Température optimale (°C)
  "optimalHumidity": 60.0,           // Humidité optimale (%)
  "luxNeeds": 3000.0,                // Besoin en lumière (lux)
  "baseGrowthRate": 2.5,             // Taux de croissance (%)
  "seedProductionRate": 50.0         // Taux de production graines
}
```

### Exemples d'espèces

#### Rose
```json
{
  "name": "Rose",
  "waterNeeds": 500.0,
  "optimalTemperature": 20.0,
  "optimalHumidity": 60.0,
  "luxNeeds": 3000.0,
  "baseGrowthRate": 2.5,
  "seedProductionRate": 50.0
}
```

#### Cactus (Conditions sèches)
```json
{
  "name": "Cactus",
  "waterNeeds": 100.0,
  "optimalTemperature": 25.0,
  "optimalHumidity": 20.0,
  "luxNeeds": 4000.0,
  "baseGrowthRate": 0.5,
  "seedProductionRate": 30.0
}
```

#### Fougère (Conditions humides)
```json
{
  "name": "Fougère",
  "waterNeeds": 800.0,
  "optimalTemperature": 18.0,
  "optimalHumidity": 80.0,
  "luxNeeds": 1000.0,
  "baseGrowthRate": 1.8,
  "seedProductionRate": 100.0
}
```

#### Chêne (Arbre)
```json
{
  "name": "Chêne",
  "waterNeeds": 1200.0,
  "optimalTemperature": 15.0,
  "optimalHumidity": 70.0,
  "luxNeeds": 5000.0,
  "baseGrowthRate": 0.3,
  "seedProductionRate": 200.0
}
```

## Récupérer une espèce

### Par nom
```bash
GET /api/species/{name}
```

**Exemple** :
```bash
curl http://localhost:8080/api/species/Rose
```

**Réponse** :
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

### Lister toutes les espèces
```bash
GET /api/species
```

**Exemple** :
```bash
curl http://localhost:8080/api/species
```

**Réponse** :
```json
[
  {
    "id": "507f1f77bcf86cd799439011",
    "name": "Rose",
    ...
  },
  {
    "id": "507f1f77bcf86cd799439012",
    "name": "Cactus",
    ...
  }
]
```

## Mettre à jour une espèce

### Modifier les besoins
```bash
PUT /api/species/{name}
```

**Exemple** :
```bash
curl -X PUT http://localhost:8080/api/species/Rose \
  -H "Content-Type: application/json" \
  -d '{
    "waterNeeds": 600.0,
    "optimalTemperature": 21.0,
    "optimalHumidity": 65.0,
    "luxNeeds": 3200.0,
    "baseGrowthRate": 2.7,
    "seedProductionRate": 55.0
  }'
```

## Supprimer une espèce

**Attention** : Supprimera aussi toutes les plantes associées
```bash
DELETE /api/species/{name}
```

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/species/Rose
```

## Interprétation des attributs

### Water Needs
**Impact** : Critère pour évaluer la santé

- Cactus (100ml) : Plante très résistante
- Fougère (800ml) : Plante très humide
- Chêne (1200ml) : Grand arbre

### Optimal Temperature
**Plage recommandée** : 15°C à 30°C

- Fougère (18°C) : Préfère le froid
- Rose (20°C) : Tempérée
- Cactus (25°C) : Préfère chaud

### Optimal Humidity
**Plage recommandée** : 20% à 90%

- Cactus (20%) : Très sec
- Rose (60%) : Modérée
- Fougère (80%) : Très humide

### Lux Needs
**Plage recommandée** : 500 à 6000 lux

- Fougère (1000lux) : Ombre partielle
- Rose (3000lux) : Soleil modéré
- Chêne (5000lux) : Plein soleil

### Base Growth Rate
**Taux de croissance en %/jour**

- Cactus (0.5%) : Très lente
- Fougère (1.8%) : Modérée
- Rose (2.5%) : Rapide
- Chicorée (5.0%) : Très rapide

### Seed Production Rate
**Graines produites au cycle**

- Cactus (30) : Faible production
- Rose (50) : Production modérée
- Chêne (200) : Production massive
- Fougère (100) : Production importante

## Guide des bonnes pratiques

### À faire

- Créer des espèces avec des noms clairs et descriptifs
- Garder les paramètres cohérents avec la réalité botanique
- Tester les espèces avant de les utiliser en simulation

### À éviter

- Utiliser des valeurs extrêmes irréalistes
- Créer deux espèces avec le même nom
- Modifier une espèce sans vérifier les plantes associées

## Recommandations pour créer réaliste

### Analyse botanique

1. **Research** : Consulter sources fiables sur l'espèce
2. **Normalisation** : Adapter aux échelles GreenDesk (0-10000 pour lux, etc.)
3. **Test** : Créer des plantes et vérifier comportement
4. **Ajustement** : Affiner après observation

### Exemple : Créer une Orchidée

**Recherche** :
- Température : 20-25°C
- Humidité : 70-80%
- Lumière : Indirecte, 1000-2000 lux
- Eau : Modérée, 200-300ml
- Croissance : Lente, 1-2cm/an

**Création** :
```json
{
  "name": "Orchidée",
  "waterNeeds": 250.0,
  "optimalTemperature": 22.0,
  "optimalHumidity": 75.0,
  "luxNeeds": 1500.0,
  "baseGrowthRate": 1.2,
  "seedProductionRate": 20.0
}
```

## Questions fréquentes

**Q: Puis-je créer deux espèces avec quasiment les mêmes paramètres ?**

A: Oui, mais donnez-leur des noms différents pour les distinguer. Exemple : "Rose Rouge" et "Rose Blanche"

**Q: Quel est le pas optimal pour les valeurs ?**

A: Utilisez des valeurs avec une décimale pour plus de précision (2.5 au lieu de 2 ou 3).

**Q: Puis-je avoir des espèces avec besoins très différents ?**

A: Oui, cela permet de simuler la diversité écologique. Parfait pour une forêt variée.

---

**Prêt ?** Créez vos premières espèces puis consultez le guide [Gestion des plantes](plants.md) !
