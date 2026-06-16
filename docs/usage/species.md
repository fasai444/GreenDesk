# Usage - Espèces

## Créer une espèce

```bash
curl -X POST http://localhost:8080/api/species \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Tomate",
    "optimalWaterNeeds":55,
    "optimalTemperature":22,
    "optimalHumidity":60,
    "optimalLuxNeeds":300,
    "baseGrowthRate":1.2,
    "seedProductionRate":0.8
  }'
```

## Lire les espèces

```bash
curl -s http://localhost:8080/api/species
curl -s http://localhost:8080/api/species/Tomate
```

## Mettre à jour (remplacement)

```bash
curl -X PUT http://localhost:8080/api/species/<SPECIES_ID> \
  -H "Content-Type: application/json" \
  -d '{ ... }'
```

## Supprimer

```bash
curl -X DELETE http://localhost:8080/api/species/<SPECIES_ID>
```
