# Guide de démarrage

## Pré-requis

Avant de commencer, assurez-vous d'avoir installé :

- **Java 21** ou supérieur
- **Docker** et **Docker Compose** (recommandé)
- **Git**
- **Gradle 9.2.0** (optionnel, gradlew fourni)

## Installation rapide

### Option 1 : Docker Compose (Recommandé ⭐)

La façon la plus simple de démarrer :

```bash
cd GreenDesk
docker-compose up --build
```

Cela va :
- Construire l'application
- Lancer MongoDB
- Lancer Mongo Express
- Lancer GreenDesk

### Option 2 : Gradle (Développement local)

```bash
cd GreenDesk
./gradlew bootRun
```

Vous devez avoir MongoDB en local ou via Docker :

```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

## Accès à l'application

Une fois démarrée, accédez à :

| Service | URL |
|---------|-----|
| Application | http://localhost:8080 |
| Swagger/OpenAPI | http://localhost:8080/swagger-ui.html |
| API JSON | http://localhost:8080/v3/api-docs |
| Mongo Express | http://localhost:8081 |

## Premier test : Créer une espèce

### Via la CLI (curl)

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

### Via Swagger

1. Ouvrez http://localhost:8080/swagger-ui.html
2. Cliquez sur "POST /api/species"
3. Cliquez sur "Try it out"
4. Collez le JSON ci-dessus
5. Cliquez sur "Execute"

## Créer une plante

```bash
curl -X POST http://localhost:8080/api/plants/Rose \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rose du Jardin",
    "water": 450.0,
    "temperature": 22.0,
    "humidity": 55.0,
    "luxIntensity": 2800.0
  }'
```

## Créer une forêt

```bash
curl -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Forêt Enchantée",
    "width": 10,
    "height": 10
  }'
```

## Placer une plante dans une forêt

```bash
curl -X POST "http://localhost:8080/api/forests/{forestId}/plants/{plantId}?posX=5&posY=5"
```

## Étapes suivantes

1. **Lire les concepts** : Consultez [Concepts fondamentaux](usage/concepts.md)
2. **Explorer l'API** : Allez sur [Référence API](api/overview.md)
3. **Lancer les tests** : Exécutez `./gradlew test`
4. **Contribuer** : Consultez [Guide de contribution](contributing.md)

## Conseils utiles

- 📖 Consultez la [FAQ](faq.md) pour les problèmes courants
- 🔍 C'est votre première fois ? Lire [Concepts fondamentaux](usage/concepts.md)
- 🧪 Voulez-vous contribuer ? Lisez [Tests](testing.md)
- 🐳 Problèmes Docker ? Consultez [Docker](docker.md)

## Résolution des problèmes courants

### "Connection refused" pour MongoDB

```bash
# Vérifier que le conteneur MongoDB tourne
docker ps | grep mongodb

# Ou le relancer
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### Port déjà utilisé

```bash
# Trouver le processus utilisant le port
netstat -ano | findstr :8080

# Ou changer le port dans application.properties
server.port=8081
```

### Gradle permission denied

```bash
# Rendre gradlew exécutable
chmod +x gradlew
```

---

**Prêt à creuser ?** Consultez le [guide d'utilisation](usage/concepts.md) !
