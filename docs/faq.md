# FAQ

## Où trouver la doc API interactive ?

Sur `http://localhost:8080/swagger-ui/index.html` une fois l'application démarrée.

## Pourquoi certains endpoints sont sous `/plants` et d'autres sous `/api/...` ?

Pour compatibilité historique. `PlantController` expose les deux préfixes (`/plants` et `/api/plants`).

## Comment réinitialiser l'environnement Docker ?

```bash
docker compose down -v
docker compose up -d --build
```

## Comment vérifier rapidement la qualité du code avant push ?

```bash
./gradlew clean check
```

## Où sont les rapports de test et couverture ?

- tests: `build/reports/tests/test/index.html`
- couverture: `build/reports/jacoco/test/html/index.html`

## Quelle est la base utilisée par défaut ?

MongoDB Atlas (cf. `application.properties`), avec option Docker locale via variables d'environnement.

## L'application ne démarre pas, que vérifier ?

1. port 8080 libre
2. accès MongoDB valide
3. Java 21 actif
4. logs `./gradlew bootRun --info`
