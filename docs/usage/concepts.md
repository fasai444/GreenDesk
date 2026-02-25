# Concepts métier

## Entités principales

- **Species**: profil agronomique (eau, température, humidité, lumière, croissance)
- **Plant**: instance vivante liée à une espèce
- **Forest**: grille 2D de plantes positionnées
- **SeasonCycle**: cycle de saison propre à une forêt
- **Effect**: modificateur appliqué à une plante
- **Stimulus**: événement appliqué à une forêt
- **PlantAlert**: alerte opérationnelle sur une plante
- **SensorReading**: mesure capteur horodatée

## État d'une plante

`PlantState` est calculé à partir des écarts aux besoins de l'espèce.

États principaux:

- `HEALTHY`
- `STRESSED`
- `DORMANT`
- `DISEASED`

## Flux métier simplifié

1. Créer des espèces
2. Créer des plantes
3. Créer une forêt
4. Positionner les plantes dans la forêt
5. Appliquer effets/stimulus
6. Lire KPI et alertes
7. Piloter ROI via `/api/greenhouse/*`

## Règles notables

- Une position de forêt ne peut pas contenir 2 plantes
- Certaines règles empêchent les clones trop similaires dans une même forêt
- Les erreurs de conflit remontent en `409` pour ces cas
