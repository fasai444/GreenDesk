# Plant Placement Optimizer - Complete Guide

## Overview

The Plant Placement Optimizer uses a **Genetic Algorithm** to find optimal plant arrangements in your forest based on:
- **Species compatibility** (companion planting)
- **Resource competition** (water, light)
- **Disease resistance spacing**

---

## 🏗️ Architecture

### Backend Components

#### 1. Domain Models (`entities/placement/`)

- **PlacementConstraint.java**: Defines compatibility rules between species
- **PlacementSolution.java**: Represents a complete placement (chromosome)
- **PlantPosition.java**: Individual plant position with species info
- **HeatmapCell.java**: Cell data for visualization

#### 2. Service (`services/`)

**PlacementOptimizationService.java** - Core genetic algorithm:
- **Population Size**: 100 solutions
- **Generations**: 200 iterations
- **Mutation Rate**: 15%
- **Crossover Rate**: 70%
- **Elite Count**: 5 best solutions preserved

##### Fitness Function Components:

```java
fitness = companionScore + diseaseSpacing + edgeBonus - resourceCompetition
```

1. **Companion Planting** (distance ≤ 3 cells):
   - Tomato + Basil: +0.8
   - Carrot + Tomato: +0.5
   - Tomato + Potato: -0.7

2. **Disease Resistance**:
   - Same species < 3 cells: penalty
   - Same species 3-5 cells: bonus

3. **Resource Competition** (distance < 2):
   - Penalty for overcrowding

4. **Edge Preference**:
   - Bonus for central placement

#### 3. REST API (`controllers/placement/`)

**PlacementOptimizationController.java** provides:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/placement/optimize` | POST | Run optimization algorithm |
| `/api/placement/optimize-and-apply/{forestId}` | POST | Optimize and apply to forest |
| `/api/placement/heatmap/{forestId}?species=X` | GET | Generate placement heatmap |
| `/api/placement/suggest/{forestId}?species=X` | GET | Get best single position |

---

## 🎨 Frontend (placement-optimizer.html)

### Features:

1. **Forest Selection**: Choose existing or create new forest
2. **Plant Selection**: Multi-select plants to optimize
3. **Genetic Algorithm**: Visual feedback during optimization
4. **Heatmap Overlay**: Color-coded optimal zones
5. **Drag-and-Drop**: (Canvas displays optimized positions)
6. **Live Stats**: Fitness score, generation count

### Visual Legend:

- 🔵 Blue = Tomato
- 🟢 Green = Basil
- 🟠 Orange = Carrot
- 🟣 Purple = Other species
- 🌈 Gradient = Heatmap (green=good, red=bad)

---

## 📖 Usage Instructions

### Step 1: Start the Application

```bash
cd GreenDesk
./gradlew bootRun
```

Access: **http://localhost:8080/placement-optimizer.html**

### Step 2: Create Test Data

```bash
# Create species
curl -X POST http://localhost:8080/api/species \
-H "Content-Type: application/json" \
-d '{
  "name": "Tomato",
  "optimalWaterNeeds": 200,
  "optimalTemperature": 22,
  "optimalHumidity": 60,
  "optimalLuxNeeds": 1500,
  "baseGrowthRate": 1.5,
  "seedProductionRate": 0.4
}'

curl -X POST http://localhost:8080/api/species \
-H "Content-Type: application/json" \
-d '{
  "name": "Basil",
  "optimalWaterNeeds": 150,
  "optimalTemperature": 20,
  "optimalHumidity": 55,
  "optimalLuxNeeds": 1200,
  "baseGrowthRate": 1.2,
  "seedProductionRate": 0.3
}'

# Create forest
curl -X POST http://localhost:8080/api/forests \
-H "Content-Type: application/json" \
-d '{
  "name": "Optimization Test Forest",
  "width": 10,
  "height": 10
}'

# Create plants (replace SPECIES_ID)
curl -X POST "http://localhost:8080/plants/create?name=Tomato1&speciesId=TOMATO_SPECIES_ID"
curl -X POST "http://localhost:8080/plants/create?name=Basil1&speciesId=BASIL_SPECIES_ID"
```

### Step 3: Use the UI

1. Select your forest from dropdown
2. Check plants you want to place
3. Click **"Run Genetic Algorithm"**
4. Wait for optimization (shows progress)
5. Review fitness score and placement preview
6. Click **"Apply to Forest"** to save

### Step 4: View Heatmap

1. Select a species (e.g., "Tomato")
2. Click **"Show Heatmap"**
3. Green areas = optimal placement
4. Red areas = poor placement
5. Gray areas = occupied

---

## 🧪 Testing

Run the test suite:

```bash
./gradlew test --tests TestPlacementOptimization
```

**Tests included:**
- ✅ Multi-species optimization
- ✅ Single species with spacing
- ✅ Heatmap generation
- ✅ Boundary conditions
- ✅ Empty list validation
- ✅ Fitness score comparison
- ✅ Position bounds checking

---

## 🔧 API Examples

### 1. Optimize Placement

```bash
curl -X POST http://localhost:8080/api/placement/optimize \
-H "Content-Type: application/json" \
-d '{
  "plantIds": ["plant1_id", "plant2_id", "plant3_id"],
  "forestWidth": 10,
  "forestHeight": 10
}'
```

**Response:**
```json
{
  "fitnessScore": 12.45,
  "placements": [
    {"plantId": "plant1_id", "speciesName": "Tomato", "x": 2, "y": 3},
    {"plantId": "plant2_id", "speciesName": "Basil", "x": 3, "y": 3}
  ],
  "message": "Optimization completed successfully"
}
```

### 2. Get Heatmap

```bash
curl "http://localhost:8080/api/placement/heatmap/FOREST_ID?species=Tomato"
```

**Response:**
```json
{
  "forestId": "abc123",
  "species": "Tomato",
  "width": 10,
  "height": 10,
  "heatmap": [
    {"x": 0, "y": 0, "score": 0.65, "recommendedSpecies": "Tomato"},
    {"x": 0, "y": 1, "score": 0.72, "recommendedSpecies": "Tomato"}
  ]
}
```

### 3. Get Best Position Suggestion

```bash
curl "http://localhost:8080/api/placement/suggest/FOREST_ID?species=Basil"
```

**Response:**
```json
{
  "x": 4,
  "y": 5,
  "score": 0.89,
  "species": "Basil"
}
```

---

## 🎯 Genetic Algorithm Details

### Initialization
- Generate 100 random placement solutions
- Ensure no position overlaps within each solution

### Evolution Loop (200 generations)
1. **Fitness Evaluation**: Calculate score for each solution
2. **Selection**: Tournament selection (size=5)
3. **Crossover**: Combine two parent solutions (70% chance)
4. **Mutation**: Randomly swap positions (15% chance)
5. **Elitism**: Keep 5 best solutions unchanged

### Termination
- After 200 generations, return best solution
- Early stopping if fitness converges (optional enhancement)

---

## 🌟 Customization

### Add New Species Compatibility

Edit `PlacementOptimizationService.java`:

```java
private Map<String, Map<String, Double>> initializeCompatibilityMatrix() {
    Map<String, Map<String, Double>> matrix = new HashMap<>();

    // Add your species
    Map<String, Double> pepperCompat = new HashMap<>();
    pepperCompat.put("Tomato", 0.6);  // Good companion
    pepperCompat.put("Basil", 0.7);   // Great companion
    matrix.put("Pepper", pepperCompat);

    return matrix;
}
```

### Adjust Algorithm Parameters

```java
private static final int POPULATION_SIZE = 150;      // More solutions
private static final int MAX_GENERATIONS = 300;      // More iterations
private static final double MUTATION_RATE = 0.20;    // More mutation
```

### Modify Fitness Weights

```java
// In calculateFitness() method
fitness += compatScore * (4.0 - distance) * 2.0; // Increase companion weight
fitness -= (3.0 - distance) * 3.0;               // Increase spacing penalty
```

---

## 📊 Performance Notes

- **10x10 forest, 10 plants**: ~2-3 seconds
- **15x15 forest, 30 plants**: ~5-8 seconds
- **20x20 forest, 50 plants**: ~15-20 seconds

Complexity: O(P² × G × N)
- P = population size
- G = generations
- N = number of plants

---

## 🐛 Troubleshooting

### "No valid plants found"
- Ensure plants exist in database
- Check plant IDs are correct

### Heatmap not showing
- Verify forest has existing plants
- Check species name matches exactly

### Low fitness scores
- Check compatibility matrix has data for your species
- Increase forest size for more placement options
- Reduce number of plants

### Optimization takes too long
- Reduce `POPULATION_SIZE` or `MAX_GENERATIONS`
- Use smaller forest dimensions
- Optimize fewer plants at once

---

## 🚀 Future Enhancements

1. **Multi-objective optimization** (maximize yield + minimize disease)
2. **Seasonal considerations** (sun path, shade patterns)
3. **Growth prediction** (account for mature plant sizes)
4. **Water zone clustering** (group by irrigation needs)
5. **Real-time drag-and-drop** placement with live fitness updates
6. **Export optimization results** to CSV/PDF
7. **3D visualization** with plant heights

---

## 📝 File Structure

```
GreenDesk/
├── src/main/java/org/example/
│   ├── entities/placement/
│   │   ├── PlacementConstraint.java
│   │   ├── PlacementSolution.java
│   │   ├── PlantPosition.java
│   │   └── HeatmapCell.java
│   ├── services/
│   │   └── PlacementOptimizationService.java
│   └── controllers/placement/
│       └── PlacementOptimizationController.java
├── src/main/resources/static/
│   └── placement-optimizer.html
└── src/test/java/org/example/
    └── TestPlacementOptimization.java
```

---

## 📚 References

- **Genetic Algorithms**: Holland, J. H. (1992)
- **Companion Planting**: Riotte, L. (1998)
- **Optimization Heuristics**: Michalewicz, Z. (1996)

---

## ✨ Credits

Developed for **GreenDesk v4.0**
Algorithm: Genetic Algorithm with elitism
Language: Java 21, Spring Boot 3.3.3
Frontend: Vanilla JavaScript + Canvas API

---

**Happy Optimizing! 🌿**
