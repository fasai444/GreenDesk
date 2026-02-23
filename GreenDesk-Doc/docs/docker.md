# Guide Docker

Documentation complète pour déployer GreenDesk avec Docker et Docker Compose.

## Concepts Docker

### Image

Une image Docker est un modèle immuable contenant :
- Système d'exploitation
- Dépendances
- Application
- Configuration

### Container

Un container est une instance d'une image - l'application en exécution.

### Docker Compose

Orchestre plusieurs containers (GreenDesk + MongoDB + Mongo Express).

## Installation Docker

### Windows/Mac

Téléchargez [Docker Desktop](https://www.docker.com/products/docker-desktop)

### Linux

```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

## Structure Docker de GreenDesk

### Fichiers

```
greendesk/
├── Dockerfile                # Image GreenDesk
├── docker-compose.yml       # Orchestration
├── DOCKER.md               # Aide Docker
└── test-docker.sh          # Script test
```

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/greendesk.jar app.jar

EXPOSE 8080

ENV SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/greendesk

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Explications** :
- `FROM` : Image de base (Java 21 Alpine)
- `WORKDIR` : Répertoire de travail
- `COPY` : Copier le JAR
- `EXPOSE` : Port 8080
- `ENV` : Variable MongoDB
- `ENTRYPOINT` : Commande de démarrage

### docker-compose.yml

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:6.0
    container_name: mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    networks:
      - greendesk-network

  mongo-express:
    image: mongo-express:latest
    container_name: mongo-express
    environment:
      ME_CONFIG_MONGODB_ADMINUSERNAME: admin
      ME_CONFIG_MONGODB_ADMINPASSWORD: password
      ME_CONFIG_MONGODB_URL: mongodb://admin:password@mongodb:27017/
    ports:
      - "8081:8081"
    depends_on:
      - mongodb
    networks:
      - greendesk-network

  greendesk:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: greendesk
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://admin:password@mongodb:27017/greendesk?authSource=admin
    ports:
      - "8080:8080"
    depends_on:
      - mongodb
    networks:
      - greendesk-network

networks:
  greendesk-network:
    driver: bridge

volumes:
  mongo_data:
```

## Démarrage et arrêt

### Démarrage complet

```bash
cd greendesk

# Build et démarrage des 3 services
docker-compose up --build

# Ou sans logs interactifs
docker-compose up -d --build
```

### Vérifier services

```bash
docker-compose ps

# Résultat :
# NAME          STATUS    PORTS
# mongodb       Up        27017/tcp
# mongo-express Up        8081/tcp
# greendesk     Up        8080/tcp
```

### Logs

```bash
# Tous les services
docker-compose logs -f

# Service spécifique
docker-compose logs -f greendesk

# Derniers logs
docker-compose logs --tail=50 greendesk
```

### Arrêt

```bash
# Arrêter tous les services
docker-compose down

# Supprimer volumes aussi
docker-compose down -v
```

## Accès aux services

| Service | URL/Port |
|---------|----------|
| GreenDesk | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Mongo Express | http://localhost:8081 |
| MongoDB | mongodb://admin:password@localhost:27017 |

## Commandes Docker utiles

### Images

```bash
# Lister images
docker images

# Supprimer image
docker rmi greendesk:latest

# Construire image
docker build -t greendesk:latest .
```

### Containers

```bash
# Lister containers
docker ps -a

# Arrêter container
docker stop greendesk

# Redémarrer container
docker restart greendesk

# Supprimer container
docker rm greendesk

# Logs container
docker logs greendesk
```

### Réseau

```bash
# Lister réseaux
docker network ls

# Inspecter réseau
docker network inspect greendesk-network
```

### Volumes

```bash
# Lister volumes
docker volume ls

# Inspecter volume
docker volume inspect mongo_data

# Supprimer volume non utilisé
docker volume prune
```

## Dépannage Docker

### "Cannot connect to MongoDB"

```bash
# Vérifier que mongodb tourne
docker-compose ps | grep mongodb

# Redémarrer
docker-compose restart mongodb

# Vérifier logs
docker-compose logs mongodb
```

### "Port already in use"

```bash
# Trouver processus
netstat -ano | findstr :8080

# Changer port dans docker-compose.yml
ports:
  - "9000:8080"  # Au lieu de 8080:8080
```

### GreenDesk ne démarre pas

```bash
# Vérifier logs
docker-compose logs greendesk

# Erreur courante : JAR not found
# Solution : Rebuild image
docker-compose build --no-cache greendesk

# Ou relancer complètement
docker-compose down -v
docker-compose up --build
```

### MongoDB permission denied

```bash
# Vérifier volume permissions
docker volume inspect mongo_data

# Fixer permissions (Linux)
sudo chown -R 999:999 /var/lib/docker/volumes/mongo_data
```

## Build personnalisé

### Construire JAR

```bash
./gradlew clean build bootJar

# JAR disponible : build/libs/greendesk-1.0-SNAPSHOT.jar
```

### Dockerfile optimisé (multistage)

```dockerfile
# Stage 1 : Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY . .

RUN ./gradlew clean bootJar

# Stage 2 : Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/build/libs/greendesk*.jar app.jar

EXPOSE 8080

ENV SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/greendesk

ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
```

**Avantages** :
- Image plus petite (pas du JDK)
- Meilleure sécurité
- Démarrage plus rapide

## Variantes de déploiement

### Développement (avec Live Reload)

```yaml
# docker-compose.dev.yml
services:
  greendesk:
    build:
      context: .
      dockerfile: Dockerfile
    volumes:
      - .:/app  # Hot reload
    environment:
      SPRING_PROFILES_ACTIVE: dev
```

```bash
docker-compose -f docker-compose.dev.yml up
```

### Production (Minimal)

```yaml
# docker-compose.prod.yml
services:
  greendesk:
    image: greendesk:latest
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATA_MONGODB_URI: mongodb+srv://user:pass@cluster.mongodb.net/greendesk
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M
```

## Monitoring Docker

### Stats containers

```bash
docker stats

# Résultat :
# CONTAINER    CPU%    MEM USAGE/LIMIT
# greendesk    0.5%    128MiB/512MiB
# mongodb      1.2%    256MiB/512MiB
```

### Événements

```bash
# Watch événements en temps réel
docker events --filter type=container
```

## Best practices Docker

### À faire

- Utiliser Alpine pour images plus petites
- Limiter ressources (CPU, mémoire)
- Utiliser healthchecks
- Sauvegarder données via volumes
- Utiliser .dockerignore
- Versioner les images

### À éviter

- Lancer services importants en root
- Stocker données en layer image
- Négliger les logs
- Oublier docker network
- Utiliser latest dans production

### Healthcheck

```yaml
services:
  greendesk:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 10s
      timeout: 5s
      retries: 3
```

## Kubernetes futur

Pour déployer à large échelle :

```bash
# Créer image
docker build -t myregistry/greendesk:1.0 .

# Push vers registry
docker push myregistry/greendesk:1.0

# Déployer avec Kubernetes
kubectl apply -f k8s/deployment.yaml
```

## Tests Docker

### Script de test

```bash
./test-docker.sh

# Ou manuellement :

# 1. Démarrer
docker-compose up -d

# 2. Attendre démarrage
sleep 10

# 3. Tester endpoints
curl http://localhost:8080/api/species

# 4. Vérifier Mongo Express
curl http://localhost:8081

# 5. Arrêter
docker-compose down
```

## Troubleshooting avancé

### Inspecter dans container

```bash
# Ouvrir shell dans container
docker-compose exec greendesk bash

# Ou
docker-compose exec greendesk sh

# Commandes utiles
ps aux
ls -la
curl http://localhost:8080/health
```

### Générer dump MongoDB

```bash
docker-compose exec mongodb mongodump \
  -u admin \
  -p password \
  --authenticationDatabase admin \
  --out /data/backup
```

### Logs détaillés

```bash
# Tous les logs des 100 dernières lignes
docker-compose logs --tail=100 greendesk

# Avec timestamps
docker-compose logs --timestamps greendesk

# Filtrer par période
docker-compose logs --since 10m greendesk
```

---

**Déploiement réussi ?** Consultez [FAQ](faq.md) pour plus d'aide !
