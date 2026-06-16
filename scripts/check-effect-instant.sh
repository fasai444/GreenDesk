#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"

need() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "[ERREUR] commande manquante: $1"
    exit 1
  }
}

need curl
need python3

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

echo "[INFO] API: $BASE"
curl -fsS "$BASE/api/species" >/dev/null

seed="$(date +%s)"
species_name="InstantCheckSpecies_${seed}"
plant_name="InstantCheckPlant_${seed}"

cat > "$tmp/species_payload.json" <<JSON
{
  "name": "$species_name",
  "optimalWaterNeeds": 400.0,
  "optimalTemperature": 20.0,
  "optimalHumidity": 60.0,
  "optimalLuxNeeds": 2500.0,
  "baseGrowthRate": 2.0,
  "seedProductionRate": 0.5
}
JSON

curl -fsS -X POST "$BASE/api/species" -H "Content-Type: application/json" --data @"$tmp/species_payload.json" > "$tmp/species.json"

species_id="$(python3 - <<'PY' "$tmp/species.json"
import json, sys
print(json.load(open(sys.argv[1])).get('id',''))
PY
)"

curl -fsS -X POST "$BASE/plants/create?name=${plant_name}&speciesId=${species_id}&water=400&temperature=20&humidity=60&lux=2500" > "$tmp/plant.json"

plant_id="$(python3 - <<'PY' "$tmp/plant.json"
import json, sys
print(json.load(open(sys.argv[1])).get('id',''))
PY
)"

cat > "$tmp/effect_payload.json" <<JSON
{
  "name": "InstantHeat_${seed}",
  "description": "Effet test immédiat",
  "durationHours": 2,
  "temperatureModifier": 8.0,
  "waterModifier": -25.0,
  "humidityModifier": -10.0,
  "luxModifier": 300.0,
  "stressReduction": -0.2,
  "growthRateModifier": 0.0
}
JSON

curl -fsS -X POST "$BASE/api/effects" -H "Content-Type: application/json" --data @"$tmp/effect_payload.json" > "$tmp/effect.json"
effect_id="$(python3 - <<'PY' "$tmp/effect.json"
import json, sys
print(json.load(open(sys.argv[1])).get('id',''))
PY
)"

curl -fsS "$BASE/plants/${plant_id}/status" > "$tmp/before.json"
curl -fsS -X POST "$BASE/api/plants/${plant_id}/effects/${effect_id}" > "$tmp/apply.json"
curl -fsS "$BASE/plants/${plant_id}/status" > "$tmp/after.json"

python3 - <<'PY' "$tmp/before.json" "$tmp/after.json" "$tmp/apply.json"
import json, sys

before=json.load(open(sys.argv[1]))
after=json.load(open(sys.argv[2]))
apply_resp=json.load(open(sys.argv[3]))

bs=before.get("sensors",{})
as_=after.get("sensors",{})

def delta(k):
    try:
        return round(float(as_.get(k,0))-float(bs.get(k,0)), 4)
    except Exception:
        return None

print("=== Vérification effet immédiat ===")
print("plantId:", after.get("plantId"))
print("effectAppliedId:", apply_resp.get("effectId", apply_resp.get("id")))
print("state before -> after:", before.get("plantState"), "->", after.get("plantState"))
print("stress before -> after:", before.get("stressIndex"), "->", after.get("stressIndex"))
print("sensor deltas:", {
    "temperature": delta("temperature"),
    "waterLevel": delta("waterLevel"),
    "humidity": delta("humidity"),
    "lux": delta("lux")
})
print("activeEffects before -> after:", len(before.get("activeEffects",[])), "->", len(after.get("activeEffects",[])))

has_delta = any([
    delta("temperature") not in (0, None),
    delta("waterLevel") not in (0, None),
    delta("humidity") not in (0, None),
    delta("lux") not in (0, None),
])

before_effects=len(before.get("activeEffects",[]))
after_effects=len(after.get("activeEffects",[]))

if has_delta:
  print("verdict: OK (diff immédiate visible)")
elif after_effects > before_effects:
  print("verdict: ATTENTION (effet attaché mais pas de delta capteur)")
  print("hint: redémarre l'application pour charger la dernière version du code puis relance ce script")
else:
  print("verdict: ECHEC (ni attachement ni delta détecté)")

PY