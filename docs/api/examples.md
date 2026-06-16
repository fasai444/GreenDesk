# API - Exemples de scénarios

## Scénario 1 - Créer une espèce puis une plante

```bash
# 1) créer espèce
curl -X POST http://localhost:8080/api/species \
  -H "Content-Type: application/json" \
  -d '{"name":"Basilic","optimalWaterNeeds":45,"optimalTemperature":24,"optimalHumidity":55,"optimalLuxNeeds":280,"baseGrowthRate":1.1,"seedProductionRate":0.9}'

# 2) lister espèces et récupérer l'id
curl -s http://localhost:8080/api/species

# 3) créer plante
curl -X POST "http://localhost:8080/api/plants/create?name=Basilic-A&speciesId=<SPECIES_ID>&water=44&temperature=23&humidity=54&lux=290"
```

## Scénario 2 - Créer forêt et y placer une plante

```bash
# 1) créer forêt
curl -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d '{"name":"Zone-Nord","width":8,"height":8}'

# 2) ajouter plante dans la grille
curl -X POST http://localhost:8080/api/forests/<FOREST_ID>/plants \
  -H "Content-Type: application/json" \
  -d '{"plantId":"<PLANT_ID>","x":2,"y":4}'
```

## Scénario 3 - Effets et KPI greenhouse

```bash
# 1) voir effets
curl -s http://localhost:8080/api/effects

# 2) appliquer effet
curl -X POST http://localhost:8080/api/plants/<PLANT_ID>/effects/<EFFECT_ID>

# 3) lire impact live
curl -s "http://localhost:8080/api/greenhouse/live-effects?limit=10"

# 4) lire ROI global
curl -s "http://localhost:8080/api/greenhouse/roi?hours=24"

# 5) classement ROI des forêts
curl -s "http://localhost:8080/api/greenhouse/roi/forests?limit=10&hours=24"
```

## Scénario 4 - Tick capteurs greenhouse

```bash
curl -X POST http://localhost:8080/api/greenhouse/sensor-stream/tick \
  -H "Content-Type: application/json" \
  -d '{"forestId":"<FOREST_ID>","profile":"normal"}'
```

## Scénario 5 - Simulation écosystème

```bash
curl -X POST http://localhost:8080/api/ecosystem/simulate/5
curl -s http://localhost:8080/api/ecosystem/cells
```

## Scénario 6 - Alertes

```bash
# 1) lire alertes actives d'une plante
curl -s "http://localhost:8080/plants/<PLANT_ID>/alerts?active=true"

# 2) acquitter une alerte
curl -X POST http://localhost:8080/alerts/<ALERT_ID>/ack
```
