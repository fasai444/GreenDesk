# Usage — Forêts et saisons

## Créer une forêt

```bash
curl -X POST http://localhost:8080/api/forests \
  -H "Content-Type: application/json" \
  -d '{"name":"Forêt A","width":10,"height":10}'
```

## Lire les forêts

```bash
curl -s http://localhost:8080/api/forests
curl -s http://localhost:8080/api/forests/<FOREST_ID>
```

## Ajouter une plante à une position

```bash
curl -X POST http://localhost:8080/api/forests/<FOREST_ID>/plants \
  -H "Content-Type: application/json" \
  -d '{"plantId":"<PLANT_ID>","x":3,"y":5}'
```

## Lister plantes d'une forêt

```bash
curl -s http://localhost:8080/api/forests/<FOREST_ID>/plants
```

## Retirer une plante de la grille

```bash
curl -X DELETE "http://localhost:8080/api/forests/<FOREST_ID>/plants?x=3&y=5"
```

## Supprimer une forêt

```bash
curl -X DELETE http://localhost:8080/api/forests/<FOREST_ID>
```

## Cycle de saisons

### Créer un cycle

```bash
curl -X POST http://localhost:8080/api/forests/<FOREST_ID>/season-cycle
```

### Lire le cycle

```bash
curl -s http://localhost:8080/api/forests/<FOREST_ID>/season-cycle
```

### Avancer le cycle

```bash
curl -X POST http://localhost:8080/api/forests/<FOREST_ID>/season-cycle/advance \
  -H "Content-Type: application/json" \
  -d '{"monthsElapsed":3}'
```

### Supprimer le cycle

```bash
curl -X DELETE http://localhost:8080/api/forests/<FOREST_ID>/season-cycle
```
