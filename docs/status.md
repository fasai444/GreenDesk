# Statut du projet

## État global

- Application opérationnelle en local et Docker
- API REST documentée via Swagger
- Qualité CI activée (build/tests/couverture)

## Couverture (dernier rapport JaCoCo local)

- LINE: `81.04%`
- BRANCH: `52.47%`
- CLASS: `98.18%`

Seuils exigés:

- LINE `>= 70%`
- BRANCH `>= 45%`
- CLASS `>= 90%`

## CI/CD

Workflow principal: `.github/workflows/gradle.yml`

Il exécute:

- checkout
- setup JDK 21
- `./gradlew clean test jacocoTestReport`
- upload artifacts (tests + couverture)

## Rapport intégré

- Rapport d'amélioration **Greenhouse Ops**: [reports/greenhouse-ops-report.md](reports/greenhouse-ops-report.md)
- Points clés: normalisation des paramètres (`limit`, `hours`), validation/normalisation `forestId/profile`, erreurs `400` JSON structurées
- Validation ciblée: `8 tests passés / 0 échec` sur `GreenhouseOpsControllerTest`

## Risques techniques connus

- Certaines branches métiers restent complexes (simulation/stimulus)
- La configuration Mongo Atlas en dur doit être remplacée par variables secrètes en production

## Prochaines priorités recommandées

1. Renforcer tests sur `StimulusService` et `EcosystemController`
2. Uniformiser les réponses d'erreur JSON sur tous les contrôleurs
3. Ajouter pipeline de publication docs si besoin de site public
