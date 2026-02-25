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
