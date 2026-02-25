# Usage — Effets, stimulus, alertes, simulation

## Effets

### Lister le catalogue

```bash
curl -s http://localhost:8080/api/effects
curl -s "http://localhost:8080/api/effects?custom=true"
```

### Créer un effet personnalisé

```bash
curl -X POST http://localhost:8080/api/effects \
  -H "Content-Type: application/json" \
  -d '{"name":"Brume","description":"Humidité douce","durationHours":6,"humidityModifier":0.1}'
```

### Appliquer un effet à une plante

```bash
curl -X POST http://localhost:8080/api/plants/<PLANT_ID>/effects/<EFFECT_ID>
```

### Lire les effets d'une plante

```bash
curl -s http://localhost:8080/api/plants/<PLANT_ID>/effects
curl -s http://localhost:8080/api/plants/<PLANT_ID>/effects/active
```

### Retirer un effet

```bash
curl -X DELETE http://localhost:8080/api/plants/effects/<PLANT_EFFECT_ID>
```

## Stimulus

```bash
curl -X POST http://localhost:8080/api/stimuli \
  -H "Content-Type: application/json" \
  -d '{"type":"WATERING","forestId":"<FOREST_ID>","intensity":0.6,"durationHours":4}'
```

## Alertes

```bash
curl -s "http://localhost:8080/plants/<PLANT_ID>/alerts?active=true"
curl -X POST http://localhost:8080/alerts/<ALERT_ID>/ack
```

## Simulation écosystème

```bash
curl -X POST http://localhost:8080/api/ecosystem/tick
curl -X POST http://localhost:8080/api/ecosystem/simulate/10
curl -s http://localhost:8080/api/ecosystem/cells

curl -X POST http://localhost:8080/api/ecosystem/tick/<FOREST_ID>
curl -X POST http://localhost:8080/api/ecosystem/simulate/<FOREST_ID>/10
curl -s http://localhost:8080/api/ecosystem/cells/<FOREST_ID>
```
