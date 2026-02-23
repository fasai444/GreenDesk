# Installation complète

## Configuration système

### Minimale
- Java 21+
- 4 GB RAM
- 500 MB espace disque

### Recommandée
- Java 21+
- 8 GB RAM
- 2 GB espace disque
- Docker & Docker Compose
- MongoDB 6.0+

## Installation de MongoDB

### Option 1 : MongoDB Atlas (Cloud) - Recommandé

1. Créez un compte sur [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Créez un cluster gratuit
3. Générez une chaîne de connexion
4. Mise à jour du `application.properties` :

```properties
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/greendesk?retryWrites=true&w=majority
```

### Option 2 : MongoDB Local (Docker)

```bash
docker run -d \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  --name mongodb \
  mongo:6.0
```

Configuration dans `application.properties` :

```properties
spring.data.mongodb.uri=mongodb://admin:password@localhost:27017/greendesk?authSource=admin
```

### Option 3 : MongoDB Local (Natif)

**Linux/Mac** :

```bash
brew install mongodb-community
brew services start mongodb-community
```

**Windows** :

1. Téléchargez depuis [mongodb.com](https://www.mongodb.com/try/download/community)
2. Exécutez l'installateur
3. Lancez le service MongoDB

Configuration dans `application.properties` :

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/greendesk
```

## Installation docker

### Docker

**Windows/Mac** : Téléchargez [Docker Desktop](https://www.docker.com/products/docker-desktop)

**Linux** :

```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

### Docker Compose

```bash
docker compose version
# Si pas installé, installez Docker Desktop (inclus)
```

## Installation du projet

### 1. Cloner le repository

```bash
git clone https://github.com/yourteam/greendesk.git
cd greendesk
```

### 2. Initialiser le projet

#### Avec Docker Compose (Recommandé)

```bash
# Construire et lancer tous les services
docker-compose up --build

# Ou en arrière-plan
docker-compose up -d --build
```

#### Avec Gradle

```bash
# Construire le projet
./gradlew build

# Lancer l'application (requiert MongoDB en local)
./gradlew bootRun
```

## Configuration

### Configuration par défaut

Le fichier `src/main/resources/application.properties` contient :

```properties
# Server
server.port=8080
server.servlet.context-path=/

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/greendesk
spring.data.mongodb.auto-index-creation=true

# Logging
logging.level.root=INFO
logging.level.org.springframework.web=DEBUG

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

### Variables d'environnement

Vous pouvez surcharger les paramètres via variables d'environnement :

```bash
export SPRING_DATA_MONGODB_URI=mongodb://myhost:27017/greendesk
export SERVER_PORT=9000
./gradlew bootRun
```

## Vérification de l'installation

### 1. Vérifier que l'application tourne

```bash
curl http://localhost:8080/swagger-ui.html
```

### 2. Vérifier MongoDB

```bash
curl http://localhost:8081
# (Mongo Express)
```

### 3. Tester l'API

```bash
curl http://localhost:8080/api/species

# Ou avec Swagger :
# http://localhost:8080/swagger-ui.html
```

## Structure des répertoires

```
greendesk/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── controllers/      # Contrôleurs REST
│   │   │   ├── services/         # Logique métier
│   │   │   ├── repositories/     # Accès données
│   │   │   └── entities/         # Models JPA
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/org/example/     # Tests unitaires
├── GreenDesk-Doc/                # Documentation
├── Dockerfile                    # Image Docker
├── docker-compose.yml           # Orchestration
├── build.gradle                 # Configuration Gradle
└── README.md
```

## Dépendances principales

| Dépendance | Version | Usage |
|-----------|---------|-------|
| Spring Boot | 3.3.3 | Framework web |
| Spring Data MongoDB | Latest | ORM MongoDB |
| JUnit 5 | 5.10.0 | Tests |
| Lombok | Latest | Annotation processing |
| OpenAPI | 2.5.0 | Documentation API |

## Démarrage

### Développement

```bash
./gradlew bootRun
```

### Production (Docker)

```bash
docker-compose up --build -d
```

### Tests

```bash
./gradlew test
```

### Build pour production

```bash
./gradlew clean build bootJar
# Jar disponible dans build/libs/
```

## Ports utilisés

| Service | Port |
|---------|------|
| Application GreenDesk | 8080 |
| Mongo Express | 8081 |
| MongoDB | 27017 |

## Dépannage

### Erreur de connexion MongoDB

- Vérifiez que MongoDB tourne : `docker ps`
- Vérifiez l'URI de connexion dans `application.properties`
- Vérifiez les credentials

### Port déjà utilisé

```bash
# Trouver le processus
netstat -ano | findstr :8080

# Changer le port
export SERVER_PORT=8090
./gradlew bootRun
```

### Gradle permission denied

```bash
chmod +x gradlew
```

## Prochaines étapes

1. Configuration complète
2. Consultez [Concepts fondamentaux](usage/concepts.md)
3. Lancez les [exemples d'API](api/examples.md)
4. Exécutez les [tests](testing.md)

---

Vous êtes prêt ! Consultez le [guide de démarrage](getting-started.md) pour votre premier test.
