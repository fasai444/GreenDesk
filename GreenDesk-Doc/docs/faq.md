# Questions fréquemment posées (FAQ)

## Installation et démarrage

### Q: Quel est le prérequis minimum pour lancer GreenDesk ?

A: Vous avez besoin de :
- Java 21+
- 4 GB de RAM
- 500 MB d'espace disque

Pour la façon la plus simple, utilisez Docker (recommandé).

### Q: Puis-je lancer GreenDesk sans Docker ?

A: Oui ! Suivez ces étapes :

```bash
# 1. Installer MongoDB localement
# (Voir guide installation)

# 2. Configurer la connexion
# Éditer src/main/resources/application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/greendesk

# 3. Lancer l'application
./gradlew bootRun
```

### Q: MongoDB n'est pas installé. Où le télécharger ?

A: Plusieurs options :

**Option 1 : MongoDB Atlas (Cloud, gratuit)**
- Allez sur https://www.mongodb.com/cloud/atlas
- Créez un compte gratuit
- Générez une chaîne de connexion
- Utilisez-la dans `application.properties`

**Option 2 : Docker (recommandé)**
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

**Option 3 : Installé localement**
- Windows/Mac : Téléchargez depuis mongodb.com
- Ubuntu : `sudo apt install mongodb`

### Q: Quel est le port par défaut ?

A: Par défaut c'est le **8080**. Vous pouvez le changer :

```properties
# application.properties
server.port=9000
```

### Q: Erreur "Connection refused". Qu'est-ce qui ne va pas ?

A: 99% du temps, c'est MongoDB qui n'est pas en cours d'exécution.

```bash
# Vérifier MongoDB est en cours d'exécution
docker ps | grep mongo
# ou
mongo --version

# Si arrêté, le redémarrer
docker start mongodb
```

## Premiers pas
### Q: Comment créer mon premier écosystème ?

A: Suivez ces 4 étapes :

```bash
# 1. Créer une espèce
curl -X POST http://localhost:8080/api/species \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rose",
    "waterNeeds": 500,
    "optimalTemperature": 20,
    "optimalHumidity": 60,
    "luxNeeds": 3000,
    "baseGrowthRate": 2.5,
    "seedProductionRate": 50
  }'

# 2. Créer une plante
curl -X POST http://localhost:8080/api/plants/Rose \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ma Rose",
    "water": 500,
    "temperature": 20,
    "humidity": 60,
    "luxIntensity": 3000
  }'

# 3. Créer une forêt
curl -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon Jardin",
    "width": 10,
    "height": 10
  }'

# 4. Placer la plante dans la forêt
curl -X POST "http://localhost:8080/api/forests/{forestId}/plants/{plantId}?posX=5&posY=5"
```

### Q: Où trouver la documentation de l'API ?

A: Swagger est disponible à :
```
http://localhost:8080/swagger-ui.html
```

Consulter aussi [Référence API](api/overview.md) dans cette documentation.

### Q: JSON invalide quand je crée une espèce ?

A: Vérifiez le format JSON :

```bash
# ❌ Mauvais - pas de quotes
curl -d '{name: "Rose"}'

# ✅ Correct
curl -d '{"name": "Rose"}'

# ✅ Aussi valide avec Python
requests.post(url, json={"name": "Rose"})
```

## Concepts et logique

### Q: Comment est calculée la santé d'une plante ?

A: La santé est calculée en fonction de l'écart entre conditions réelles et optimales :

```
Stress = |ValeurActuelle - ValeurOptimale| / ValeurOptimale
Santé = max(0, 100 - (Stress total × 50))

État:
- HEALTHY si santé > 80%
- STRESSED si 50% < santé ≤ 80%
- DORMANT si 20% < santé ≤ 50%
- DISEASED si santé ≤ 20%
```

Utilisez ce [calculateur](https://greendesk.dev/calculator) pour tester.

### Q: Quelle est la différence entre une espèce et une plante ?

A: **Espèce** = Template réutilisable avec besoins optimaux

```json
{
  "name": "Rose",           // Espèce
  "waterNeeds": 500,        // Besoin type
  "optimalTemperature": 20  // Optimal
}
```

**Plante** = Instance spécifique d'une espèce

```json
{
  "name": "Ma Rose",         // Cette plante en particulier
  "species": "Rose",         // Basée sur espèce Rose
  "water": 450,             // Valeur actuelle (peut différer)
  "temperature": 22,        // Conditions actuelles
  "status": "HEALTHY"       // L'état calculé
}
```

### Q: Comment fonctionnent les saisons ?

A: Cycle automatique qui modifie l'environnement :

```
SPRING (Printemps) → Optimal
⬇
SUMMER (Été) → Chaud, sécher, plus lumineux
⬇
AUTUMN (Automne) → Plus froid, normal
⬇
WINTER (Hiver) → Froid, sec, peu de lumière
⬇
Retour à SPRING
```

Chaque saison multiplie/ajoute des modificateurs à l'environnement.

### Q: Puis-je avoir deux espèces avec les mêmes besoins ?

A: Oui ! Donnez-leur des noms différents :

```json
{"name": "Rose Rouge", ...}
{"name": "Rose Blanche", ...}
```

Cela permet la diversité simulée.

### Q: La variation génétique, qu'est-ce que c'est ?

A: Quand vous placez une plante dans une forêt, elle reçoit une `variationSeed` (0.8-1.2) qui ajuste légèrement ses paramètres :

```
variationSeed = 0.95 → Plante 5% plus faible
variationSeed = 1.05 → Plante 5% plus forte
```

C'est pour réalisme : même espèce, différents phénotypes.

## Utilisation API

### Q: Quelle est la différence entre PUT et POST ?

A: **POST** = Créer nouveau
```bash
POST /api/species  # Crée nouvelle espèce
```

**PUT** = Mettre à jour existant
```bash
PUT /api/species/Rose  # Modifie Rose existante
```

### Q: Comment gérer les erreurs ?

A: Regardez le code de réponse HTTP :

| Code | Signification | Action |
|------|---------------|--------|
| 200 | OK | Succès ! |
| 201 | Created | Créé avec succès |
| 400 | Bad Request | Vérifier format JSON |
| 404 | Not Found | Espèce/plante inexistante |
| 409 | Conflict | Position occupée |
| 500 | Server Error | Bug ? Signaler issue |

### Q: Puis-je avoir plusieurs effets sur une plante ?

A: Oui ! Les effets se combinent :

```bash
curl -X POST http://localhost:8080/api/plants/{id}/effects/SHADE
curl -X POST http://localhost:8080/api/plants/{id}/effects/FERTILIZER
curl -X POST http://localhost:8080/api/plants/{id}/effects/HEATING

# Résultat : 3 effets appliqués ensemble
```

### Q: Quelle est la limite d'appels à l'API ?

A: **Aucune limite actuellement** (développement). Production : À définir.

### Q: Comment supprimer une espèce sans supprimer les plantes ?

A: **Impossible actuellement**. Supprimer une espèce supprime aussi les plantes. C'est une restriction de design.

## Docker

### Q: Docker Port déjà utilisé ?

A: Changez le port dans docker-compose.yml :

```yaml
services:
  greendesk:
    ports:
      - "9000:8080"  # Au lieu de 8080:8080
```

### Q: Comment accéder à MongoDB directement ?

A: Avec mongo-express (inclus) :
```
http://localhost:8081
```

Ou via client :
```bash
docker-compose exec mongodb mongosh \
  -u admin \
  -p password \
  --authenticationDatabase admin
```

### Q: Supprimer les données Docker

A: 
```bash
# Supprimer containers + volumes
docker-compose down -v

# Cela supprimera aussi la database MongoDB !
```

## Tests

### Q: Comment exécuter les tests ?

A: 
```bash
# Tous
./gradlew test

# Spécifique
./gradlew test --tests SpeciesServiceTest

# Avec coverage
./gradlew test jacocoTestReport
```

### Q: Mes tests échouent avec "MongoDB not found"

A: Les tests utilisent une base de test. Assurez-vous que MongoDB est accessible.

## Performance

### Q: Peut-on avoir 10 000 plantes dans 1 forêt ?

A: Techniquement oui, mais pas recommandé. Optimum : 100-500 plantes.

### Q: Quelle taille peut avoir une forêt ?

A: Aucune limite formelle, mais test avec 10000x10000 : lents.

Recommandé : width/height ≤ 1000

### Q: Combien de forêts puis-je créer ?

A: Aucune limite. Limite = capacité storageMongoDB.

## Contribution

### Q: Je veux contribuer. Comment commencer ?

A: Lisez [Guide de contribution](contributing.md).

En résumé :
1. Fork le repository
2. Créer branche feature
3. Faire changements + tests
4. PR

### Q: Il y a un bug. Où le signaler ?

A: Créez une issue GitHub avec :
- Description du bug
- Étapes pour reproduire
- Logs d'erreur
- Votre environnement (OS, Java version)

### Q: Je veux ajouter une nouvelle feature. Comment proposer ?

A: Créez une issue avec le label "enhancement". Discutez avec mainteneurs avant de développer.

## Divers

### Q: Y a-t-il une interface web ?

A: Oui ! Accédez à :
```
http://localhost:8080
```

Contient outils d'administration de base.

### Q: Peut-on exporter les données ?

A: Oui, via MongoDB :
```bash
docker-compose exec mongodb mongodump \
  -u admin \
  -p password \
  --authenticationDatabase admin \
  --out /data/backup
```

### Q: Quelle est la licence ?

A: [À définir - À vérifier dans LICENSE.md]

### Q: Comment me tenir au courant des mises à jour ?

A: 
- ⭐ Star le repository
- 👁️ Watch pour notifications
- 📧 Newsletter : [email]

## Vous avez une autre question ?

- 💬 Créer une issue avec label "question"
- 📧 Contacter : support@greendesk.dev
- 🐛 Lire la [documentation complète](index.md)

---

**Pas trouvé la réponse ?** [Créer une issue](https://github.com/yourteam/greendesk/issues/new)
