#!/bin/bash

# Demo script for Plant Placement Optimizer
# This script demonstrates the complete workflow

BASE="http://localhost:8080"
API="$BASE/api"

echo "🌱 GreenDesk - Plant Placement Optimizer Demo"
echo "=============================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Create Species
echo -e "${BLUE}Step 1: Creating species...${NC}"

TOMATO_RESPONSE=$(curl -s -X POST "$API/species" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tomato",
    "optimalWaterNeeds": 200,
    "optimalTemperature": 22,
    "optimalHumidity": 60,
    "optimalLuxNeeds": 1500,
    "baseGrowthRate": 1.5,
    "seedProductionRate": 0.4
  }')

TOMATO_ID=$(echo "$TOMATO_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}✓ Tomato species created: $TOMATO_ID${NC}"

BASIL_RESPONSE=$(curl -s -X POST "$API/species" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Basil",
    "optimalWaterNeeds": 150,
    "optimalTemperature": 20,
    "optimalHumidity": 55,
    "optimalLuxNeeds": 1200,
    "baseGrowthRate": 1.2,
    "seedProductionRate": 0.3
  }')

BASIL_ID=$(echo "$BASIL_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}✓ Basil species created: $BASIL_ID${NC}"

CARROT_RESPONSE=$(curl -s -X POST "$API/species" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carrot",
    "optimalWaterNeeds": 180,
    "optimalTemperature": 18,
    "optimalHumidity": 50,
    "optimalLuxNeeds": 1300,
    "baseGrowthRate": 1.0,
    "seedProductionRate": 0.5
  }')

CARROT_ID=$(echo "$CARROT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}✓ Carrot species created: $CARROT_ID${NC}"
echo ""

# Step 2: Create Forest
echo -e "${BLUE}Step 2: Creating forest...${NC}"

FOREST_RESPONSE=$(curl -s -X POST "$API/forests" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Optimization Demo Forest",
    "width": 12,
    "height": 12
  }')

FOREST_ID=$(echo "$FOREST_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
echo -e "${GREEN}✓ Forest created: $FOREST_ID (12x12)${NC}"
echo ""

# Step 3: Create Plants
echo -e "${BLUE}Step 3: Creating plants...${NC}"

PLANT_IDS=()

for i in {1..4}; do
  PLANT_RESPONSE=$(curl -s -X POST "$BASE/plants/create?name=Tomato$i&speciesId=$TOMATO_ID")
  PLANT_ID=$(echo "$PLANT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  PLANT_IDS+=("$PLANT_ID")
  echo -e "${GREEN}✓ Created Tomato$i: $PLANT_ID${NC}"
done

for i in {1..4}; do
  PLANT_RESPONSE=$(curl -s -X POST "$BASE/plants/create?name=Basil$i&speciesId=$BASIL_ID")
  PLANT_ID=$(echo "$PLANT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  PLANT_IDS+=("$PLANT_ID")
  echo -e "${GREEN}✓ Created Basil$i: $PLANT_ID${NC}"
done

for i in {1..3}; do
  PLANT_RESPONSE=$(curl -s -X POST "$BASE/plants/create?name=Carrot$i&speciesId=$CARROT_ID")
  PLANT_ID=$(echo "$PLANT_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  PLANT_IDS+=("$PLANT_ID")
  echo -e "${GREEN}✓ Created Carrot$i: $PLANT_ID${NC}"
done

echo ""
echo -e "${YELLOW}Total plants created: ${#PLANT_IDS[@]}${NC}"
echo ""

# Step 4: Run Optimization
echo -e "${BLUE}Step 4: Running genetic algorithm optimization...${NC}"
echo -e "${YELLOW}(This may take 10-15 seconds)${NC}"

# Build JSON array of plant IDs
PLANT_IDS_JSON="["
for i in "${!PLANT_IDS[@]}"; do
  if [ $i -gt 0 ]; then
    PLANT_IDS_JSON+=","
  fi
  PLANT_IDS_JSON+="\"${PLANT_IDS[$i]}\""
done
PLANT_IDS_JSON+="]"

OPTIMIZATION_RESPONSE=$(curl -s -X POST "$API/placement/optimize" \
  -H "Content-Type: application/json" \
  -d "{
    \"plantIds\": $PLANT_IDS_JSON,
    \"forestWidth\": 12,
    \"forestHeight\": 12
  }")

FITNESS=$(echo "$OPTIMIZATION_RESPONSE" | grep -o '"fitnessScore":[^,}]*' | cut -d':' -f2)
PLACEMENTS_COUNT=$(echo "$OPTIMIZATION_RESPONSE" | grep -o '"plantId"' | wc -l)

echo -e "${GREEN}✓ Optimization complete!${NC}"
echo -e "  Fitness Score: ${YELLOW}$FITNESS${NC}"
echo -e "  Plants Placed: ${YELLOW}$PLACEMENTS_COUNT${NC}"
echo ""

# Step 5: Apply Optimization to Forest
echo -e "${BLUE}Step 5: Applying optimization to forest...${NC}"

APPLY_RESPONSE=$(curl -s -X POST "$API/placement/optimize-and-apply/$FOREST_ID" \
  -H "Content-Type: application/json" \
  -d "$PLANT_IDS_JSON")

echo -e "${GREEN}✓ Placement applied to forest${NC}"
echo "$APPLY_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$APPLY_RESPONSE"
echo ""

# Step 6: Generate Heatmap for Tomato
echo -e "${BLUE}Step 6: Generating heatmap for Tomato placement...${NC}"

HEATMAP_RESPONSE=$(curl -s "$API/placement/heatmap/$FOREST_ID?species=Tomato")
HEATMAP_CELLS=$(echo "$HEATMAP_RESPONSE" | grep -o '"x":' | wc -l)

echo -e "${GREEN}✓ Heatmap generated: $HEATMAP_CELLS cells${NC}"
echo ""

# Step 7: Get Position Suggestion
echo -e "${BLUE}Step 7: Getting optimal position suggestion for Basil...${NC}"

SUGGESTION_RESPONSE=$(curl -s "$API/placement/suggest/$FOREST_ID?species=Basil")
echo -e "${GREEN}✓ Suggestion:${NC}"
echo "$SUGGESTION_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$SUGGESTION_RESPONSE"
echo ""

# Summary
echo "=============================================="
echo -e "${GREEN}🎉 Demo Complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Open browser: ${BLUE}$BASE/placement-optimizer.html${NC}"
echo "2. Select forest: ${YELLOW}Optimization Demo Forest${NC}"
echo "3. View the optimized placement"
echo "4. Try generating heatmaps for different species"
echo ""
echo "Created resources:"
echo "  - Forest ID: $FOREST_ID"
echo "  - Tomato Species ID: $TOMATO_ID"
echo "  - Basil Species ID: $BASIL_ID"
echo "  - Carrot Species ID: $CARROT_ID"
echo "  - ${#PLANT_IDS[@]} plants"
echo ""
echo -e "${YELLOW}To clean up test data, restart the application or manually delete via API${NC}"
