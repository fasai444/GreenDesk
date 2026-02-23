# Documentation des Endpoints

```mermaid
flowchart TB
    subgraph Species
      S1[POST /api/species]
      S2[GET /api/species]
      S3[GET /api/species/{name}]
    end

    subgraph Plants
      P1[POST /api/plants/{speciesName}]
      P2[GET /api/plants]
      P3[GET /api/plants/{plantId}]
      P4[PUT /api/plants/{plantId}]
      P5[DELETE /api/plants/{plantId}]
    end

    subgraph Forests
      F1[POST /api/forests]
      F2[GET /api/forests/{forestId}/plants]
      F3[POST /api/forests/{forestId}/season/next]
    end

    subgraph Effects
      E1[POST /api/plants/{plantId}/effects/{effectName}]
      E2[GET /api/plants/{plantId}/effects]
      E3[DELETE /api/plants/{plantId}/effects/{effectName}]
    end

    S1 -->|creates| P1
    P1 -->|creates| P2
    P3 -->|manages| E1
    F3 -->|modifies| P2
    E1 -->|updates| P4
    style Species fill:#e3f2fd,stroke:#0288d1
    style Plants fill:#e8f5e9,stroke:#2e7d32
    style Forests fill:#fff3e0,stroke:#fb8c00
    style Effects fill:#f3e5f5,stroke:#6a1b9a
```

Référence complète de tous les endpoints de l'API GreenDesk.

## Endpoints Espèces (Species)

### Créer une espèce

```
POST /api/species
```

**Paramètres du corps** :
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

**Codes de réponse** :
- `201 Created` - Espèce créée avec succès
- `400 Bad Request` - Données invalides

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/species \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rose",
    "waterNeeds": 500.0,
    "optimalTemperature": 20.0,
    "optimalHumidity": 60.0,
    "luxNeeds": 3000.0,
    "baseGrowthRate": 2.5,
    "seedProductionRate": 50.0
  }'
```

---

### Lister toutes les espèces

```
GET /api/species
```

**Codes de réponse** :
- `200 OK` - Liste retournée

**Réponse** :
```json
[
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
]
```

**Exemple** :
```bash
curl http://localhost:8080/api/species
```

---

### Récupérer une espèce par nom

```
GET /api/species/{name}
```

**Paramètres** :
- `{name}` - Nom de l'espèce

**Codes de réponse** :
- `200 OK` - Espèce trouvée
- `404 Not Found` - Espèce inexistante

**Exemple** :
```bash
curl http://localhost:8080/api/species/Rose
```

---

### Mettre à jour une espèce

```
PUT /api/species/{name}
```

**Paramètres** :
- `{name}` - Nom de l'espèce

**Paramètres du corps** :
```json
{
  "waterNeeds": 550.0,
  "optimalTemperature": 21.0,
  "optimalHumidity": 65.0,
  "luxNeeds": 3200.0,
  "baseGrowthRate": 2.7,
  "seedProductionRate": 55.0
}
```

**Codes de réponse** :
- `200 OK` - Mise à jour réussie
- `404 Not Found` - Espèce inexistante

**Exemple** :
```bash
curl -X PUT http://localhost:8080/api/species/Rose \
  -H "Content-Type: application/json" \
  -d '{
    "waterNeeds": 550.0,
    "optimalTemperature": 21.0,
    "optimalHumidity": 65.0,
    "luxNeeds": 3200.0,
    "baseGrowthRate": 2.7,
    "seedProductionRate": 55.0
  }'
```

---

### Supprimer une espèce

```
DELETE /api/species/{name}
```

**Paramètres** :
- `{name}` - Nom de l'espèce

**Codes de réponse** :
- `204 No Content` - Suppression réussie
- `404 Not Found` - Espèce inexistante

**Attention** : Supprime aussi toutes les plantes associées

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/species/Rose
```

---

## Endpoints Plantes (Plants)

### Créer une plante

```
POST /api/plants/{speciesName}
```

**Paramètres** :
- `{speciesName}` - Nom de l'espèce

**Paramètres du corps** :
```json
{
  "name": "Ma Rose",
  "water": 450.0,
  "temperature": 22.0,
  "humidity": 55.0,
  "luxIntensity": 2900.0
}
```

**Codes de réponse** :
- `201 Created` - Plante créée
- `400 Bad Request` - Données invalides
- `404 Not Found` - Espèce inexistante

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/Rose \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ma Rose",
    "water": 450.0,
    "temperature": 22.0,
    "humidity": 55.0,
    "luxIntensity": 2900.0
  }'
```

---

### Lister toutes les plantes

```
GET /api/plants
```

**Codes de réponse** :
- `200 OK` - Liste retournée

**Exemple** :
```bash
curl http://localhost:8080/api/plants
```

---

### Récupérer une plante

```
GET /api/plants/{plantId}
```

**Paramètres** :
- `{plantId}` - ID de la plante

**Codes de réponse** :
- `200 OK` - Plante trouvée
- `404 Not Found` - Plante inexistante

**Exemple** :
```bash
curl http://localhost:8080/api/plants/507f1f77bcf86cd799439012
```

---

### Consulter l'état d'une plante

```
GET /api/plants/{plantId}/status
```

**Codes de réponse** :
- `200 OK` - État retourné
- `404 Not Found` - Plante inexistante

**Réponse** :
```json
{
  "id": "507f1f77bcf86cd799439012",
  "name": "Ma Rose",
  "status": "HEALTHY",
  "health": 95.5,
  "age": 5,
  "growth": 12.5
}
```

**Exemple** :
```bash
curl http://localhost:8080/api/plants/507f1f77bcf86cd799439012/status
```

---

### Mettre à jour une plante

```
PUT /api/plants/{plantId}
```

**Paramètres** :
- `{plantId}` - ID de la plante

**Paramètres du corps** :
```json
{
  "water": 550.0,
  "temperature": 21.0,
  "humidity": 62.0,
  "luxIntensity": 3100.0
}
```

**Codes de réponse** :
- `200 OK` - Mise à jour réussie
- `404 Not Found` - Plante inexistante

**Exemple** :
```bash
curl -X PUT http://localhost:8080/api/plants/507f1f77bcf86cd799439012 \
  -H "Content-Type: application/json" \
  -d '{
    "water": 550.0,
    "temperature": 21.0,
    "humidity": 62.0,
    "luxIntensity": 3100.0
  }'
```

---

### Supprimer une plante

```
DELETE /api/plants/{plantId}
```

**Codes de réponse** :
- `204 No Content` - Suppression réussie
- `404 Not Found` - Plante inexistante

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/plants/507f1f77bcf86cd799439012
```

---

## Endpoints Effets (Effects)

### Ajouter un effet

```
POST /api/plants/{plantId}/effects/{effectName}
```

**Paramètres** :
- `{plantId}` - ID de la plante
- `{effectName}` - SHADE, FERTILIZER, EXTRA_WATERING, ou HEATING

**Codes de réponse** :
- `200 OK` - Effet appliqué
- `404 Not Found` - Plante inexistante
- `400 Bad Request` - Effet invalide

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/FERTILIZER
```

---

### Lister les effets

```
GET /api/plants/{plantId}/effects
```

**Codes de réponse** :
- `200 OK` - Liste retournée
- `404 Not Found` - Plante inexistante

**Réponse** :
```json
["FERTILIZER", "EXTRA_WATERING"]
```

**Exemple** :
```bash
curl http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects
```

---

### Retirer un effet

```
DELETE /api/plants/{plantId}/effects/{effectName}
```

**Codes de réponse** :
- `200 OK` - Effet retiré
- `404 Not Found` - Plante ou effet inexistant

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/plants/507f1f77bcf86cd799439012/effects/FERTILIZER
```

---

## Endpoints Forêts (Forests)

### Créer une forêt

```
POST /api/forests
```

**Paramètres du corps** :
```json
{
  "name": "Forêt Enchantée",
  "width": 10,
  "height": 10
}
```

**Codes de réponse** :
- `201 Created` - Forêt créée
- `400 Bad Request` - Données invalides

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Forêt Enchantée",
    "width": 10,
    "height": 10
  }'
```

---

### Lister toutes les forêts

```
GET /api/forests
```

**Codes de réponse** :
- `200 OK` - Liste retournée

**Exemple** :
```bash
curl http://localhost:8080/api/forests
```

---

### Récupérer une forêt

```
GET /api/forests/{forestId}
```

**Codes de réponse** :
- `200 OK` - Forêt trouvée
- `404 Not Found` - Forêt inexistante

**Exemple** :
```bash
curl http://localhost:8080/api/forests/507f1f77bcf86cd799439014
```

---

### Supprimer une forêt

```
DELETE /api/forests/{forestId}
```

**Codes de réponse** :
- `204 No Content` - Suppression réussie
- `404 Not Found` - Forêt inexistante

**Exemple** :
```bash
curl -X DELETE http://localhost:8080/api/forests/507f1f77bcf86cd799439014
```

---

## Endpoints Plantes de Forêt

### Lister les plantes d'une forêt

```
GET /api/forests/{forestId}/plants
```

**Codes de réponse** :
- `200 OK` - Liste retournée
- `404 Not Found` - Forêt inexistante

**Exemple** :
```bash
curl http://localhost:8080/api/forests/507f1f77bcf86cd799439014/plants
```

---

### Ajouter une plante à une forêt

```
POST /api/forests/{forestId}/plants/{plantId}?posX={x}&posY={y}
```

**Paramètres** :
- `{forestId}` - ID de la forêt
- `{plantId}` - ID de la plante
- `posX` - Position X (0 à width-1)
- `posY` - Position Y (0 à height-1)

**Codes de réponse** :
- `200 OK` - Plante ajoutée
- `404 Not Found` - Forêt ou plante inexistante
- `409 Conflict` - Position occupée

**Exemple** :
```bash
curl -X POST "http://localhost:8080/api/forests/507f1f77bcf86cd799439014/plants/507f1f77bcf86cd799439012?posX=5&posY=5"
```

---

## Endpoints Saisons

### Récupérer la saison actuelle

```
GET /api/forests/{forestId}/season
```

**Codes de réponse** :
- `200 OK` - Saison retournée
- `404 Not Found` - Forêt inexistante

**Réponse** :
```json
{
  "currentSeason": "SPRING",
  "order": 1,
  "waterMultiplier": 1.0,
  "temperatureModifier": 0.0,
  "humidityModifier": 5.0,
  "luxMultiplier": 1.0
}
```

**Exemple** :
```bash
curl http://localhost:8080/api/forests/507f1f77bcf86cd799439014/season
```

---

### Avancer à la saison suivante

```
POST /api/forests/{forestId}/season/next
```

**Codes de réponse** :
- `200 OK` - Saison progressée
- `404 Not Found` - Forêt inexistante

**Exemple** :
```bash
curl -X POST http://localhost:8080/api/forests/507f1f77bcf86cd799439014/season/next
```

---

## Sommaire des Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/species` | Créer espèce |
| GET | `/api/species` | Lister espèces |
| GET | `/api/species/{name}` | Récupérer espèce |
| PUT | `/api/species/{name}` | Mettre à jour |
| DELETE | `/api/species/{name}` | Supprimer |
| POST | `/api/plants/{speciesName}` | Créer plante |
| GET | `/api/plants` | Lister plantes |
| GET | `/api/plants/{id}` | Récupérer plante |
| GET | `/api/plants/{id}/status` | État plante |
| PUT | `/api/plants/{id}` | Mettre à jour |
| DELETE | `/api/plants/{id}` | Supprimer |
| POST | `/api/plants/{id}/effects/{name}` | Ajouter effet |
| GET | `/api/plants/{id}/effects` | Lister effets |
| DELETE | `/api/plants/{id}/effects/{name}` | Retirer effet |
| POST | `/api/forests` | Créer forêt |
| GET | `/api/forests` | Lister forêts |
| GET | `/api/forests/{id}` | Récupérer forêt |
| DELETE | `/api/forests/{id}` | Supprimer forêt |
| GET | `/api/forests/{id}/plants` | Plantes forêt |
| POST | `/api/forests/{id}/plants/{pid}` | Ajouter plante |
| GET | `/api/forests/{id}/season` | Saison actuelle |
| POST | `/api/forests/{id}/season/next` | Saison suivante |

---

**Besoin d'exemples ?** Consultez [Exemples d'API](examples.md)
