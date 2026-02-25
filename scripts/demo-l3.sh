#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[ERREUR] Commande requise introuvable: $1"
    exit 1
  fi
}

require_cmd curl
require_cmd python3

tmp_dir="$(mktemp -d)"
cleanup() {
  rm -rf "$tmp_dir"
}
trap cleanup EXIT

echo "[INFO] Vérification de l'API: $BASE"
if ! curl -fsS "$BASE/api/species" >/dev/null; then
  echo "[ERREUR] API non accessible sur $BASE"
  echo "        Lance l'application puis réessaie. Exemple:"
  echo "        ./gradlew bootRun"
  exit 1
fi

rand="$(date +%s)"
species_name="L3_Demo_Species_${rand}"
forest_a_name="L3_Forest_A_${rand}"
forest_b_name="L3_Forest_B_${rand}"
plant_a_name="L3_Plant_A_${rand}"

echo "[INFO] Création d'une espèce de test"
cat > "$tmp_dir/species_payload.json" <<JSON
{
  "name": "$species_name",
  "optimalWaterNeeds": 420.0,
  "optimalTemperature": 21.0,
  "optimalHumidity": 62.0,
  "optimalLuxNeeds": 2800.0,
  "baseGrowthRate": 2.4,
  "seedProductionRate": 0.7
}
JSON

curl -fsS -X POST "$BASE/api/species" \
  -H "Content-Type: application/json" \
  --data @"$tmp_dir/species_payload.json" > "$tmp_dir/species.json"

species_id="$(python3 - <<'PY' "$tmp_dir/species.json"
import json, sys
data=json.load(open(sys.argv[1]))
print(data.get('id',''))
PY
)"

if [[ -z "$species_id" ]]; then
  echo "[ERREUR] Impossible de récupérer species_id"
  exit 1
fi

echo "[INFO] Création des forêts A et B"
cat > "$tmp_dir/forest_a_payload.json" <<JSON
{
  "name": "$forest_a_name",
  "width": 10,
  "height": 10
}
JSON

cat > "$tmp_dir/forest_b_payload.json" <<JSON
{
  "name": "$forest_b_name",
  "width": 10,
  "height": 10
}
JSON

curl -fsS -X POST "$BASE/api/forests" \
  -H "Content-Type: application/json" \
  --data @"$tmp_dir/forest_a_payload.json" > "$tmp_dir/forest_a.json"

curl -fsS -X POST "$BASE/api/forests" \
  -H "Content-Type: application/json" \
  --data @"$tmp_dir/forest_b_payload.json" > "$tmp_dir/forest_b.json"

forest_a_id="$(python3 - <<'PY' "$tmp_dir/forest_a.json"
import json, sys
data=json.load(open(sys.argv[1]))
print(data.get('id',''))
PY
)"

forest_b_id="$(python3 - <<'PY' "$tmp_dir/forest_b.json"
import json, sys
data=json.load(open(sys.argv[1]))
print(data.get('id',''))
PY
)"

if [[ -z "$forest_a_id" || -z "$forest_b_id" ]]; then
  echo "[ERREUR] Impossible de récupérer les IDs de forêt"
  exit 1
fi

echo "[INFO] Création de la plante A puis placement en forêt A"
curl -fsS -X POST "$BASE/plants/create?name=${plant_a_name}&speciesId=${species_id}&water=420&temperature=21&humidity=62&lux=2800" > "$tmp_dir/plant_a.json"

plant_a_id="$(python3 - <<'PY' "$tmp_dir/plant_a.json"
import json, sys
data=json.load(open(sys.argv[1]))
print(data.get('id',''))
PY
)"

if [[ -z "$plant_a_id" ]]; then
  echo "[ERREUR] Impossible de récupérer plant_a_id"
  exit 1
fi

cat > "$tmp_dir/place_a_payload.json" <<JSON
{
  "plantId": "$plant_a_id",
  "x": 1,
  "y": 1
}
JSON

curl -fsS -X POST "$BASE/api/forests/$forest_a_id/plants" \
  -H "Content-Type: application/json" \
  --data @"$tmp_dir/place_a_payload.json" > /dev/null

echo "[INFO] Clonage vers la forêt B"
curl -fsS -X POST "$BASE/plants/$plant_a_id/clone?forestId=$forest_b_id&x=2&y=2" > "$tmp_dir/plant_b.json"

plant_b_id="$(python3 - <<'PY' "$tmp_dir/plant_b.json"
import json, sys
data=json.load(open(sys.argv[1]))
print(data.get('id',''))
PY
)"

if [[ -z "$plant_b_id" ]]; then
  echo "[ERREUR] Impossible de récupérer plant_b_id (clone)"
  exit 1
fi

echo "[INFO] Application d'un stimulus HEATWAVE sur la forêt A"
cat > "$tmp_dir/stimulus_payload.json" <<JSON
{
  "type": "HEATWAVE",
  "forestId": "$forest_a_id",
  "intensity": 8.0,
  "durationHours": 6
}
JSON

curl -fsS -X POST "$BASE/api/stimuli" \
  -H "Content-Type: application/json" \
  --data @"$tmp_dir/stimulus_payload.json" > "$tmp_dir/stimulus.json"

echo "[INFO] Récupération des rapports /status et /compare"
curl -fsS "$BASE/plants/$plant_a_id/status" > "$tmp_dir/status_a.json"
curl -fsS "$BASE/plants/$plant_b_id/status" > "$tmp_dir/status_b.json"
curl -fsS "$BASE/plants/compare?leftId=$plant_a_id&rightId=$plant_b_id" > "$tmp_dir/compare.json"

python3 - <<'PY' "$tmp_dir/species.json" "$tmp_dir/forest_a.json" "$tmp_dir/forest_b.json" "$tmp_dir/status_a.json" "$tmp_dir/status_b.json" "$tmp_dir/compare.json"
import json, sys

species=json.load(open(sys.argv[1]))
forest_a=json.load(open(sys.argv[2]))
forest_b=json.load(open(sys.argv[3]))
sa=json.load(open(sys.argv[4]))
sb=json.load(open(sys.argv[5]))
cp=json.load(open(sys.argv[6]))

comp=cp.get('comparison', {}) if isinstance(cp, dict) else {}

print("\n=== Résumé scénario L3 ===")
print(f"Species: {species.get('name')} ({species.get('id')})")
print(f"Forest A: {forest_a.get('name')} ({forest_a.get('id')})")
print(f"Forest B: {forest_b.get('name')} ({forest_b.get('id')})")
print(f"Plant A: {sa.get('plantId')} | state={sa.get('plantState')} | stress={sa.get('stressIndex')}")
print(f"Plant B: {sb.get('plantId')} | state={sb.get('plantState')} | stress={sb.get('stressIndex')}")
print(f"A recentStimuli={len(sa.get('recentStimuli', []))} | B recentStimuli={len(sb.get('recentStimuli', []))}")
print(f"comparison.stateChanged={comp.get('stateChanged')}")
print(f"comparison.stressIndexDelta={comp.get('stressIndexDelta')}")
print(f"comparison.heightCmDelta={comp.get('heightCmDelta')}")

sensor_delta = comp.get('sensorDelta', {}) if isinstance(comp.get('sensorDelta'), dict) else {}
if sensor_delta:
    print(f"comparison.sensorDelta={sensor_delta}")

ok = True
if len(sa.get('recentStimuli', [])) == 0:
    ok = False
if sa.get('forestId') == sb.get('forestId'):
    ok = False

print("\nVerdict:", "OK (scénario L3 validé)" if ok else "INCOMPLET (vérifier les sorties ci-dessus)")
PY

echo "\n[INFO] Démo terminée."