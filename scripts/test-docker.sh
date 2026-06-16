#!/bin/bash

echo "GreenDesk Docker - Test des services"
echo "========================================"
echo ""

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Docker containers
echo "1. Verification des conteneurs..."
if docker compose ps | grep -q "Up"; then
    echo -e "${GREEN}[OK]${NC} Conteneurs en cours d'execution"
else
    echo -e "${RED}[ERREUR]${NC} Probleme avec les conteneurs"
    exit 1
fi
echo ""

# Test 2: MongoDB
echo "2. Test MongoDB..."
if docker exec greendesk-mongodb mongosh --quiet --eval "db.version()" > /dev/null 2>&1; then
    echo -e "${GREEN}[OK]${NC} MongoDB repond ($(docker exec greendesk-mongodb mongosh --quiet --eval "db.version()"))"
else
    echo -e "${RED}[ERREUR]${NC} MongoDB ne repond pas"
fi
echo ""

# Test 3: Application API
echo "3. Test API GreenDesk..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/species)
if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}[OK]${NC} API accessible (HTTP $HTTP_CODE)"
else
    echo -e "${RED}[ERREUR]${NC} API non accessible (HTTP $HTTP_CODE)"
fi
echo ""

# Test 4: Swagger
echo "4. Test Swagger UI..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui.html)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo -e "${GREEN}[OK]${NC} Swagger UI accessible"
else
    echo -e "${RED}[ERREUR]${NC} Swagger UI non accessible"
fi
echo ""

# Test 5: Mongo Express
echo "5. Test Mongo Express..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ]; then
    echo -e "${GREEN}[OK]${NC} Mongo Express accessible"
else
    echo -e "${RED}[ERREUR]${NC} Mongo Express non accessible"
fi
echo ""

# Résumé des URLs
echo "URLs d'acces"
echo "==============="
echo -e "${YELLOW}Application API:${NC}     http://localhost:8080"
echo -e "${YELLOW}Swagger UI:${NC}          http://localhost:8080/swagger-ui.html"
echo -e "${YELLOW}OpenAPI Docs:${NC}        http://localhost:8080/v3/api-docs"
echo -e "${YELLOW}Mongo Express:${NC}       http://localhost:8081 (admin/admin)"
echo -e "${YELLOW}MongoDB:${NC}             mongodb://localhost:27017"
echo ""

# Commandes utiles
echo "Commandes utiles"
echo "==================="
echo "Logs app:        docker compose logs -f app"
echo "Logs MongoDB:    docker compose logs -f mongodb"
echo "Stop all:        docker compose down"
echo "Restart app:     docker compose restart app"
echo ""
