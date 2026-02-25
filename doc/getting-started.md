# Démarrage rapide

## Prérequis

- Java 21 installé
- Docker (optionnel, recommandé pour MongoDB local)
- Port `8080` libre pour l'application

## Option A — Exécution locale (MongoDB Atlas déjà configuré)

```bash
./gradlew clean bootRun
```

Puis:

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

## Option B — Exécution full Docker

```bash
docker compose up -d --build
```

Puis:

- App: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Mongo Express: `http://localhost:8081` (`admin` / `admin`)

## Vérifications rapides

### Santé API

```bash
curl -s http://localhost:8080/ | head
```

### Liste des espèces

```bash
curl -s http://localhost:8080/api/species
```

### Vue KPI Greenhouse

```bash
curl -s http://localhost:8080/api/greenhouse/overview
```

## Arrêt

### Local

`Ctrl + C` dans le terminal

### Docker

```bash
docker compose down
```

## Suite recommandée

- [Installation détaillée](installation.md)
- [Concepts métier](usage/concepts.md)
- [Référence API](api/overview.md)
