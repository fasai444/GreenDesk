# API - Vue d'ensemble

## Base URL

`http://localhost:8080`

## Documentation interactive

`/swagger-ui/index.html`

## Groupes d'endpoints

- Home: `/`
- Espèces: `/api/species`
- Plantes: `/api/plants` et `/plants`
- Forêts: `/api/forests`
- Saisons globales: `/api/seasons`
- Effets: `/api/effects` et `/api/plants/*/effects`
- Stimulus: `/api/stimuli`
- Alertes: `/plants/{plantId}/alerts`, `/alerts/{alertId}/ack`
- Sensor readings: `/plants/{plantId}/sensor-readings`
- Écosystème: `/api/ecosystem`
- Greenhouse Ops: `/api/greenhouse`

## Formats

- Requêtes: JSON + query params selon endpoint
- Réponses: JSON (ou chaîne sur certains endpoints historiques simulation)

## Erreurs HTTP fréquentes

- `400 Bad Request`: paramètres invalides / payload invalide
- `404 Not Found`: ressource absente
- `409 Conflict`: conflit métier (position occupée, diversité)

## Format d'erreur Greenhouse Ops

Les endpoints `/api/greenhouse/*` renvoient désormais les erreurs `400` au format JSON structuré:

```json
{
	"error": "message lisible",
	"endpoint": "/api/greenhouse/...",
	"timestamp": "2026-02-25T..."
}
```

Voir le détail dans le rapport: [../reports/greenhouse-ops-report.md](../reports/greenhouse-ops-report.md)

## Références détaillées

- [Endpoints complets](endpoints.md)
- [Exemples de scénarios](examples.md)
