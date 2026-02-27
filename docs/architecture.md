# Architecture

## Vue d'ensemble

GreenDesk suit une architecture Spring Boot classique:

- `controllers`: exposition API REST
- `services`: logique métier
- `repositories`: accès MongoDB
- `entities`: modèles métier

## Diagramme d'architecture (Mermaid)

```mermaid
flowchart LR
    Client[Client / Frontend / API Consumer] --> C[Controllers]
    C --> S[Services]
    S --> R[Repositories]
    R --> M[(MongoDB)]

    S --> E[Entities]
    C --> DTO[DTOs Request/Response]
    
    classDef clientStyle fill:#e1f5ff,stroke:#01579b,stroke-width:2px
    classDef ctrlStyle fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    classDef svcStyle fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
    classDef repoStyle fill:#ffccbc,stroke:#d84315,stroke-width:2px
    classDef dbStyle fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    classDef domainStyle fill:#b2dfdb,stroke:#00695c,stroke-width:2px
    
    class Client clientStyle
    class C ctrlStyle
    class S svcStyle
    class R repoStyle
    class M dbStyle
    class E,DTO domainStyle
```

## Diagramme de classes (Mermaid)

```mermaid
classDiagram
    class Species {
        +id: String
        +name: String
        +optimalWaterNeeds: double
        +optimalTemperature: double
        +optimalHumidity: double
        +optimalLuxNeeds: double
    }

    class Plant {
        +id: String
        +name: String
        +forestId: String
        +stressIndex: double
        +evaluateState()
    }

    class Forest {
        +id: String
        +name: String
        +width: int
        +height: int
    }

    class Effect {
        +id: String
        +name: String
        +durationHours: int
    }

    class PlantEffect {
        +id: String
        +plantId: String
        +effectId: String
        +active: boolean
    }

    class SensorReading {
        +id: String
        +plantId: String
        +timestamp: LocalDateTime
        +temperature: double
        +humidity: double
        +lux: double
        +rainfall: double
    }

    class PlantAlert {
        +id: String
        +plantId: String
        +type: AlertType
        +severity: AlertSeverity
        +acknowledged: boolean
    }

    class GreenhouseOpsService {
        +getOverview()
        +getRoiInsights(hours)
        +emitSensorTick(forestId, profile)
    }

    Species "1" <-- "*" Plant : species
    Forest "1" <-- "*" Plant : contains
    Plant "1" <-- "*" PlantEffect : has
    Effect "1" <-- "*" PlantEffect : references
    Plant "1" <-- "*" SensorReading : records
    Plant "1" <-- "*" PlantAlert : alerts
    GreenhouseOpsService ..> Plant
    GreenhouseOpsService ..> Forest
    GreenhouseOpsService ..> SensorReading
    GreenhouseOpsService ..> PlantAlert
    GreenhouseOpsService ..> PlantEffect
    
    style Species fill:#c8e6c9,stroke:#2e7d32,stroke-width:2px
    style Plant fill:#bbdefb,stroke:#1565c0,stroke-width:2px
    style Forest fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    style Effect fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    style PlantEffect fill:#ffccbc,stroke:#d84315,stroke-width:2px
    style SensorReading fill:#b2dfdb,stroke:#00695c,stroke-width:2px
    style PlantAlert fill:#ffcdd2,stroke:#c62828,stroke-width:2px
    style GreenhouseOpsService fill:#e1f5ff,stroke:#01579b,stroke-width:2px
```

## Modules métier principaux

### Domaine plantes

- `Plant`, `PlantState`, `PlantEffect`
- `PlantService`, `SensorReadingService`, `PlantAlertService`

### Domaine espèces

- `Species`
- `SpeciesService`

### Domaine forêts et saisons

- `Forest`, `ForestCell`
- `Season`, `SeasonCycle`
- `ForestService`, `SeasonService`

### Domaine effets et stimulus

- `Effect`, `Stimulus`
- `EffectService`, `StimulusService`

### Domaine simulation

- `Ecosystem`, `EcosystemCell`, maladies
- `EcosystemService`

### Domaine opérations greenhouse

- `GreenhouseOpsService`
- endpoints KPI/ROI/alertes/sensor-stream

## Couche API (controllers)

Contrôleurs principaux:

- `PlantController`
- `SpeciesController`
- `ForestController`
- `ForestSeasonController`
- `EffectController`
- `StimulusController`
- `SensorReadingController`
- `PlantAlertController`
- `EcosystemController`
- `GreenhouseOpsController`

## Données et persistance

- MongoDB via Spring Data MongoDB
- Repositories typés (`PlantRepository`, `ForestRepository`, etc.)
- Initialisation de données de référence via `DataInitializer`

## Qualité et CI

- Tests JUnit/Spring
- JaCoCo avec seuils de validation dans `build.gradle`
- CI GitHub Actions pour build/tests/coverage

## Principes de conception

- Endpoints REST explicites
- Validation métier dans les services
- Réponses d'erreur HTTP cohérentes (`400`, `404`, `409` selon le cas)
- Endpoints analytics séparés (`/api/greenhouse/*`)
