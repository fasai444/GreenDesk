# Vue d'ensemble de l'API

Bienvenue dans la documentation de l'API REST GreenDesk !

## Introduction

L'API GreenDesk expose toutes les fonctionnalités via endpoints REST documentés avec **Swagger/OpenAPI**.

## Caractéristiques

- ✅ **API REST** complète et RESTful
- ✅ **JSON** pour requêtes et réponses
- ✅ **Validation** automatique des données
- ✅ **Codes HTTP** standards (200, 201, 400, 404, 409, 500)
- ✅ **Documentation interactive** avec Swagger
- ✅ **OpenAPI 3.0** conforme

## Accès à l'API

### Interface Swagger

Ouvrez dans votre navigateur :
```
http://localhost:8080/swagger-ui.html
```

Vous pouvez :
- 📖 Lire la documentation complète
- 🧪 Tester les endpoints directement
- 📋 Voir les schémas JSON

### Documention JSON (OpenAPI)

```
http://localhost:8080/v3/api-docs
```

Format machine-readable pour intégrations.

## URL de base

```
http://localhost:8080
```

## Format des requêtes et réponses

### Requête

```bash
curl -X POST http://localhost:8080/api/species \
  -H "Content-Type: application/json" \
  -d '{"name": "Rose", "waterNeeds": 500.0, ...}'
```

### Réponse

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

## Codes de réponse HTTP

| Code | Sens | Exemple |
|------|------|---------|
| **200** | OK | GET /species/Rose |
| **201** | Créé | POST /species |
| **204** | Pas de contenu | DELETE /species/{id} |
| **400** | Mauvaise requête | Données invalides |
| **404** | Non trouvé | GET /species/Inexistant |
| **409** | Conflit | Position occupée dans forêt |
| **500** | Erreur serveur | MongoDB indisponible |

## Authentification

**Actuellement** : Aucune authentification requise (développement)

**Future** : JWT tokens peuvent être ajoutés

## Limites de l'API

- ✅ Pas de limite de requêtes (actuellement)
- ✅ Réponses de taille raisonnable
- ✅ Pas de pagination actuellement

## Structure du projet

L'API est organisée par ressources :

```
/api/
├── /species          # Gestion espèces
├── /plants           # Gestion plantes
├── /forests          # Gestion forêts
└── /forests/{id}/    # Opérations forêt
    ├── /plants       # Plantes dans forêt
    └── /season       # Gestion saisons
```

## Ressources principales

### Espèces (Species)
- Créer : `POST /api/species`
- Lire : `GET /api/species`
- Mettre à jour : `PUT /api/species/{name}`
- Supprimer : `DELETE /api/species/{name}`

### Plantes (Plants)
- Créer : `POST /api/plants/{speciesName}`
- Lire : `GET /api/plants`
- Mettre à jour : `PUT /api/plants/{plantId}`
- Supprimer : `DELETE /api/plants/{plantId}`
- Effets : `POST/GET/DELETE /api/plants/{plantId}/effects/{effectName}`

### Forêts (Forests)
- Créer : `POST /api/forests`
- Lire : `GET /api/forests`
- Supprimer : `DELETE /api/forests/{forestId}`
- Plantes : `GET /api/forests/{forestId}/plants`
- Ajouter plante : `POST /api/forests/{forestId}/plants/{plantId}`
- Saisons : `GET /api/forests/{forestId}/season`
- Avancer saison : `POST /api/forests/{forestId}/season/next`

## Erreurs courantes

### 404 Not Found
```json
{
  "timestamp": "2024-02-23T14:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Species not found: Rose"
}
```

**Solution** : Vérifier le nom exact avec `GET /api/species`

### 409 Conflict
```json
{
  "timestamp": "2024-02-23T14:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Position already occupied"
}
```

**Solution** : Utiliser position différente ou supprimer plante existante

### 400 Bad Request
```json
{
  "timestamp": "2024-02-23T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "waterNeeds must be positive"
}
```

**Solution** : Vérifier format JSON et types de données

## Clients recommandés

### Ligne de commande
```bash
# curl (inclus par défaut)
curl -X GET http://localhost:8080/api/species

# httpie (plus lisible)
http GET localhost:8080/api/species
```

### GUI
- [Postman](https://www.postman.com) - Le plus populaire
- [Insomnia](https://insomnia.rest) - Alternative moderne
- [REST Client (VSCode extension)](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)

### Programmation
```java
// Java
RestTemplate restTemplate = new RestTemplate();
Species species = restTemplate.getForObject(
  "http://localhost:8080/api/species/Rose",
  Species.class
);
```

```python
# Python
import requests
response = requests.get("http://localhost:8080/api/species")
species = response.json()
```

```javascript
// JavaScript
const response = await fetch('http://localhost:8080/api/species');
const species = await response.json();
```

## Bonnes pratiques

- ✅ Toujours vérifier les codes de réponse HTTP
- ✅ Traiter les erreurs gracieusement
- ✅ Utiliser headers `Content-Type: application/json`
- ✅ Valider les données avant envoi
- ✅ Respecter les types de données (float vs int)

## Prochaines étapes

- 📝 Consultez [Endpoints](endpoints.md) pour liste complète
- 🔨 Voir [Exemples](examples.md) pour cas d'usage réels
- 🏗️ En savoir plus sur [Architecture](../architecture.md)

---

**Besoin d'aide ?** Consultez [Swagger](http://localhost:8080/swagger-ui.html) ou [FAQ](../faq.md)
