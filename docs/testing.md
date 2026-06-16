# Tests et qualité

## Objectifs qualité

GreenDesk impose des seuils de couverture via JaCoCo (`jacocoTestCoverageVerification`) :

- `LINE >= 70%`
- `BRANCH >= 45%`
- `CLASS >= 90%`

## Commandes principales

### Exécuter tous les tests

```bash
./gradlew test
```

### Vérifier build + qualité complète

```bash
./gradlew clean check
```

### Générer les rapports JaCoCo

```bash
./gradlew test jacocoTestReport
```

## Emplacements des rapports

- Tests: `build/reports/tests/test/index.html`
- Couverture HTML: `build/reports/jacoco/test/html/index.html`
- Couverture XML: `build/reports/jacoco/test/jacocoTestReport.xml`

## Couverture actuelle (dernier rapport généré)

Calculée depuis les compteurs globaux JaCoCo (`LINE: covered=1590, missed=372`, `BRANCH: covered=329, missed=298`, `CLASS: covered=54, missed=1`):

- LINE: `81.04%`
- BRANCH: `52.47%`
- CLASS: `98.18%`

## Diagramme du passage de tests + JaCoCo

```mermaid
flowchart TD
    A[Commande: ./gradlew clean check] --> B[Task test]
    B --> C[Exécution JUnit / Spring Tests]
    C --> D[Résultats tests XML + HTML]
    B --> E[Task jacocoTestReport]
    E --> F[Rapports JaCoCo XML + HTML]
    F --> G[jacocoTestCoverageVerification]
    G --> H{Seuils respectés ?}
    H -->|Oui| I[Build OK]
    H -->|Non| J[Build FAIL]
    
    style A fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    style B,E fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style C,F,G fill:#bbdefb,stroke:#1565c0,stroke-width:2px
    style D fill:#b2dfdb,stroke:#00695c,stroke-width:2px
    style H fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    style I fill:#c8e6c9,stroke:#2e7d32,stroke-width:3px
    style J fill:#ffcdd2,stroke:#c62828,stroke-width:3px
```

## Diagramme couverture JaCoCo

```mermaid
pie showData
    title Couverture actuelle (dernier rapport)
    "LINE" : 81.04
    "BRANCH" : 52.47
    "CLASS" : 98.18
```

## Stratégie de tests recommandée

1. Tests unitaires des services métiers critiques
2. Tests contrôleurs sur chemins succès et erreur
3. Tests ciblés sur branches sensibles (ROI, alertes, simulation)
4. Vérification coverage avant merge

## Bonnes pratiques

- Garder les tests déterministes
- Isoler les dépendances via mocks quand pertinent
- Couvrir les cas de validation et les erreurs métier
- Contrôler les régressions via `./gradlew check`

## Rapport de campagne ciblée

- Rapport Greenhouse Ops: [reports/greenhouse-ops-report.md](reports/greenhouse-ops-report.md)
- Résultat de la campagne WebMvc ciblée: `8 tests passés / 0 échec`
