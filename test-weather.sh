#!/bin/bash
# Script pour tester les alertes météo

echo "=== Test des alertes météo ==="

# Attendre que l'application démarre
sleep 10

# URL de base (adapter le port si nécessaire)
BASE_URL="http://localhost:8081"

echo "1. Test de l'endpoint des alertes météo (devrait être vide au départ)"
curl -s "${BASE_URL}/api/weather/alerts" | jq '.' || curl -s "${BASE_URL}/api/weather/alerts"

echo -e "\n2. Envoi d'un webhook de test (alerte de chaleur)"
WEBHOOK_DATA='{
  "event_id": "test-heatwave-001",
  "type": "heatwave",
  "coords": [48.8566, 2.3522],
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
  "severity": "high",
  "details": {
    "temperature": 38.5,
    "description": "Vague de chaleur exceptionnelle"
  }
}'

echo "Données du webhook:"
echo "$WEBHOOK_DATA" | jq '.' || echo "$WEBHOOK_DATA"

RESPONSE=$(curl -s -X POST "${BASE_URL}/api/weather/webhook" \
  -H "Content-Type: application/json" \
  -d "$WEBHOOK_DATA")

echo "Réponse du webhook:"
echo "$RESPONSE" | jq '.' || echo "$RESPONSE"

echo -e "\n3. Vérification des alertes après le webhook"
sleep 2
curl -s "${BASE_URL}/api/weather/alerts" | jq '.' || curl -s "${BASE_URL}/api/weather/alerts"

echo -e "\n4. Test avec filtre 'activeOnly=true'"
curl -s "${BASE_URL}/api/weather/alerts?activeOnly=true" | jq '.' || curl -s "${BASE_URL}/api/weather/alerts?activeOnly=true"

echo -e "\n=== Test terminé ==="