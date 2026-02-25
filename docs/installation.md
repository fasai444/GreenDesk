# Installation détaillée

## 1) Cloner le projet

```bash
git clone <votre-url-repo>
cd GreenDesk
```

## 2) Vérifier les versions

```bash
java -version
./gradlew -v
```

Attendu:

- Java 21
- Gradle wrapper opérationnel

## 3) Configuration MongoDB

Par défaut, `src/main/resources/application.properties` pointe sur MongoDB Atlas.

Pour un usage Docker local, utilisez les variables d'environnement (déjà dans `docker-compose.yml`) :

- `SPRING_DATA_MONGODB_URI`
- `SPRING_DATA_MONGODB_DATABASE`

## 4) Lancer en mode développement

```bash
./gradlew bootRun
```

## 5) Build, tests, couverture

```bash
./gradlew clean check
```

Ce pipeline exécute:

- tests JUnit
- rapport JaCoCo
- vérification de seuils JaCoCo

## 6) Générer uniquement le rapport de couverture

```bash
./gradlew test jacocoTestReport
```

Rapports:

- HTML: `build/reports/jacoco/test/html/index.html`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`

## 7) Exécution Docker

```bash
docker compose up -d --build
```

Contrôles:

```bash
docker compose ps
docker compose logs -f app
```

## 8) Ports utilisés

- `8080`: GreenDesk API
- `8081`: Mongo Express
- `27017`: MongoDB

## 9) Dépannage rapide

### Port 8080 déjà pris

```bash
lsof -i :8080
kill -9 <pid>
```

### Échec de connexion Mongo

- vérifier URI/DB
- vérifier service Mongo démarré
- vérifier credentials

### Échec tests

- exécuter `./gradlew test --info`
- consulter `build/reports/tests/test/index.html`
