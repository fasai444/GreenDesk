@echo off
REM Script pour tester les alertes météo sur Windows

echo === Test des alertes météo ===

REM Attendre que l'application démarre
timeout /t 5 /nobreak > nul

REM URL de base (adapter le port si nécessaire)
set BASE_URL=http://localhost:8081
if "%WEATHER_WEBHOOK_SECRET%"=="" (
  echo WEATHER_WEBHOOK_SECRET doit etre defini.
  exit /b 1
)

echo 1. Test de l'endpoint des alertes météo (devrait être vide au départ)
curl -s "%BASE_URL%/api/weather/alerts"

echo.
echo 2. Envoi d'un webhook de test (alerte de chaleur)
set WEBHOOK_DATA={\"event_id\":\"test-heatwave-001\",\"type\":\"heatwave\",\"coords\":[48.8566,2.3522],\"timestamp\":\"2026-04-05T10:00:00.000Z\",\"severity\":\"high\",\"details\":{\"temperature\":38.5,\"description\":\"Vague de chaleur exceptionnelle\"}}

echo Données du webhook:
echo %WEBHOOK_DATA%

echo.
curl -s -X POST "%BASE_URL%/api/weather/webhook" -H "Content-Type: application/json" -H "X-Webhook-Secret: %WEATHER_WEBHOOK_SECRET%" -d "%WEBHOOK_DATA%"

echo.
echo 3. Vérification des alertes après le webhook
timeout /t 2 /nobreak > nul
curl -s "%BASE_URL%/api/weather/alerts"

echo.
echo 4. Test avec filtre 'activeOnly=true'
curl -s "%BASE_URL%/api/weather/alerts?activeOnly=true"

echo.
echo === Test terminé ===
pause
