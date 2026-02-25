# Usage — Plantes

## Créer une plante

### Minimal

```bash
curl -X POST "http://localhost:8080/api/plants/create?name=Tomate-01&speciesId=<SPECIES_ID>"
```

### Avec capteurs initiaux

```bash
curl -X POST "http://localhost:8080/api/plants/create?name=Tomate-02&speciesId=<SPECIES_ID>&water=55&temperature=22&humidity=60&lux=300"
```

## Lire les plantes

```bash
curl -s http://localhost:8080/api/plants
curl -s http://localhost:8080/api/plants/<PLANT_ID>
curl -s http://localhost:8080/api/plants/<PLANT_ID>/state
curl -s http://localhost:8080/api/plants/<PLANT_ID>/status
```

## Comparer deux plantes

```bash
curl -s "http://localhost:8080/api/plants/compare?leftId=<ID1>&rightId=<ID2>"
```

## Cloner une plante dans une forêt

```bash
curl -X POST "http://localhost:8080/api/plants/<PLANT_ID>/clone?forestId=<FOREST_ID>&x=1&y=2"
```

## Mettre à jour capteurs

```bash
curl -X PUT "http://localhost:8080/api/plants/<PLANT_ID>?water=50&temperature=23&humidity=58&lux=320"
```

## Supprimer

```bash
curl -X DELETE "http://localhost:8080/api/plants/<PLANT_ID>"
```

## Sensor readings

### Ajouter une mesure

```bash
curl -X POST http://localhost:8080/plants/<PLANT_ID>/sensor-readings \
  -H "Content-Type: application/json" \
  -d '{"timestamp":"2026-02-25T10:00:00","temperature":22.3,"humidity":58.2,"lux":315.0,"rainfall":0.0}'
```

### Lire mesures

```bash
curl -s http://localhost:8080/plants/<PLANT_ID>/sensor-readings
curl -s "http://localhost:8080/plants/<PLANT_ID>/sensor-readings?from=2026-02-25T00:00:00&to=2026-02-25T23:59:59"
curl -s http://localhost:8080/plants/<PLANT_ID>/sensor-readings/latest
```
