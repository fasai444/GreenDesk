# Docker

## Services définis

Le `docker-compose.yml` démarre trois services:

- `app` : application GreenDesk (Spring Boot)
- `mongodb` : base MongoDB
- `mongo-express` : interface d'administration MongoDB

## Démarrage rapide

```bash
docker compose up -d --build
```

## Vérifier l'état

```bash
docker compose ps
docker compose logs -f app
```

## Endpoints utiles

| Service | URL | Credentials |
|---|---|---|
| API | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | - |
| Mongo Express | http://localhost:8081 | `admin` / `admin` |
| MongoDB | mongodb://localhost:27017 | `admin` / `adminpass` |

## Variables d'environnement injectées à l'app

Depuis `docker-compose.yml`:

- `SPRING_DATA_MONGODB_URI=mongodb://admin:adminpass@mongodb:27017/greendesk?authSource=admin`
- `SPRING_DATA_MONGODB_DATABASE=greendesk`

## Build image application

Le `Dockerfile` est en multi-stage:

1. stage build (`gradle:8.5-jdk21`) → `gradle clean build -x test`
2. stage runtime (`eclipse-temurin:21-jre`) → exécution du JAR

## Commandes d'exploitation

### Redémarrer un service

```bash
docker compose restart app
```

### Reconstruire uniquement l'app

```bash
docker compose build app
docker compose up -d app
```

### Accéder à Mongo shell

```bash
docker exec -it greendesk-mongodb mongosh -u admin -p adminpass
```

### Arrêter

```bash
docker compose down
```

### Arrêter + supprimer données persistées

```bash
docker compose down -v
```

## Dépannage

### App indisponible

```bash
docker compose logs --tail=200 app
docker compose logs --tail=200 mongodb
```

### Repartir proprement

```bash
docker compose down -v
docker compose up -d --build
```
