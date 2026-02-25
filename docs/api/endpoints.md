# API — Endpoints complets

## Home

- `GET /` → redirection vers `home.html`

## Espèces (`/api/species`)

- `GET /api/species`
- `GET /api/species/{name}`
- `POST /api/species`
- `PUT /api/species/{id}`
- `DELETE /api/species/{id}`
- `DELETE /api/species`

## Plantes (`/api/plants` et `/plants`)

- `POST /api/plants/create` (query: `name`, `speciesId`, optionnels `water`, `temperature`, `humidity`, `lux`)
- `GET /api/plants`
- `GET /api/plants/{id}`
- `GET /api/plants/{id}/state`
- `GET /api/plants/{id}/status`
- `GET /api/plants/compare` (query: `leftId`, `rightId`)
- `POST /api/plants/{plantId}/clone` (query: `forestId|targetForestId`, `x`, `y`)
- `PUT /api/plants/{id}` (query optionnels: `water`, `temperature`, `humidity`, `lux`)
- `DELETE /api/plants/{id}`
- `DELETE /api/plants`

## Sensor readings (`/plants/{plantId}/sensor-readings`)

- `POST /plants/{plantId}/sensor-readings`
- `GET /plants/{plantId}/sensor-readings` (query optionnels: `from`, `to`)
- `GET /plants/{plantId}/sensor-readings/latest`

## Forêts (`/api/forests`)

- `POST /api/forests`
- `GET /api/forests`
- `GET /api/forests/{forestId}`
- `POST /api/forests/{forestId}/plants`
- `GET /api/forests/{forestId}/plants`
- `DELETE /api/forests/{forestId}/plants` (query: `x`, `y`)
- `DELETE /api/forests/{forestId}`

## Saisons

### Catalogue global

- `GET /api/seasons`

### Cycle de saison d'une forêt

- `POST /api/forests/{forestId}/season-cycle`
- `GET /api/forests/{forestId}/season-cycle`
- `POST /api/forests/{forestId}/season-cycle/advance` (body: `monthsElapsed`)
- `DELETE /api/forests/{forestId}/season-cycle`

## Effets (`/api`)

- `GET /api/effects` (query optionnel: `custom=true|false`)
- `POST /api/effects`
- `POST /api/plants/{plantId}/effects/{effectId}`
- `GET /api/plants/{plantId}/effects`
- `GET /api/plants/{plantId}/effects/active`
- `DELETE /api/plants/effects/{plantEffectId}`

## Stimulus (`/api/stimuli`)

- `POST /api/stimuli`

## Alertes

- `GET /plants/{plantId}/alerts` (query optionnel: `active=true|false`)
- `POST /alerts/{alertId}/ack`

## Écosystème (`/api/ecosystem`)

- `POST /api/ecosystem/tick`
- `POST /api/ecosystem/simulate/{n}`
- `GET /api/ecosystem/cells`
- `POST /api/ecosystem/simulate/{forestId}/{n}`
- `GET /api/ecosystem/cells/{forestId}`
- `POST /api/ecosystem/tick/{forestId}`

## Greenhouse Ops (`/api/greenhouse`)

- `GET /api/greenhouse/overview`
- `GET /api/greenhouse/live-effects` (query: `limit`, default `20`)
- `GET /api/greenhouse/alerts` (query: `hours` default `24`, `limit` default `20`)
- `GET /api/greenhouse/roi` (query: `hours` default `24`)
- `GET /api/greenhouse/roi/forests` (query: `limit` default `20`, `hours` default `24`)
- `POST /api/greenhouse/sensor-stream/tick` (body: `{ "forestId": "...", "profile": "..." }`)
