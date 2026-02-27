# Plant Placement Optimizer - Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                          │
│                   (placement-optimizer.html)                     │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Forest   │  │  Plant   │  │ Optimize │  │ Heatmap  │       │
│  │ Selector │  │  List    │  │  Button  │  │  Viewer  │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│                                                                  │
│  ┌────────────────────────────────────────────────────┐        │
│  │           Canvas Visualization Layer               │        │
│  │  • Grid overlay    • Plant markers                 │        │
│  │  • Heatmap colors  • Optimization preview          │        │
│  └────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
                             ▼ HTTP REST API
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND (Spring Boot)                       │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              PlacementOptimizationController             │  │
│  │  • POST /api/placement/optimize                          │  │
│  │  • POST /api/placement/optimize-and-apply/{id}           │  │
│  │  • GET  /api/placement/heatmap/{id}?species=X            │  │
│  │  • GET  /api/placement/suggest/{id}?species=X            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           PlacementOptimizationService                   │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────┐     │  │
│  │  │        Genetic Algorithm Engine                │     │  │
│  │  │  • initializePopulation()                      │     │  │
│  │  │  • calculateFitness()                          │     │  │
│  │  │  • tournamentSelection()                       │     │  │
│  │  │  • crossover()                                 │     │  │
│  │  │  • mutate()                                    │     │  │
│  │  │  • evolvePopulation()                          │     │  │
│  │  └────────────────────────────────────────────────┘     │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────┐     │  │
│  │  │        Heatmap Generator                       │     │  │
│  │  │  • generateHeatmap()                           │     │  │
│  │  │  • calculatePositionScore()                    │     │  │
│  │  └────────────────────────────────────────────────┘     │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────┐     │  │
│  │  │    Compatibility Matrix Manager                │     │  │
│  │  │  • getCompatibilityScore()                     │     │  │
│  │  │  • Tomato + Basil: +0.8                        │     │  │
│  │  │  • Tomato + Potato: -0.7                       │     │  │
│  │  └────────────────────────────────────────────────┘     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Domain Models                          │  │
│  │  • PlacementSolution (chromosome)                        │  │
│  │  • PlantPosition (gene)                                  │  │
│  │  • PlacementConstraint (rules)                           │  │
│  │  • HeatmapCell (visualization data)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                             ▼                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Existing Services & Repos                   │  │
│  │  • PlantRepository • ForestRepository                    │  │
│  │  • PlantService    • ForestService                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE (MongoDB)                          │
│  • plants collection   • forests collection                      │
│  • species collection  • effects collection                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Flow

### Optimization Request Flow

```
User clicks "Run Optimization"
        │
        ▼
[Frontend] Collects selected plant IDs
        │
        ▼
POST /api/placement/optimize {plantIds, width, height}
        │
        ▼
[Controller] Validates request
        │
        ▼
[Service] Retrieves plant objects from database
        │
        ▼
[GA Engine] Initializes 100 random solutions (population)
        │
        ▼
[GA Engine] Loop 200 generations:
        │   ├─ Calculate fitness for each solution
        │   ├─ Sort by fitness (best first)
        │   ├─ Tournament selection (pick parents)
        │   ├─ Crossover (combine parents → offspring)
        │   ├─ Mutation (random position swaps)
        │   └─ Replace population with new generation
        ▼
[GA Engine] Return best solution (highest fitness)
        │
        ▼
[Controller] Format response {fitness, placements}
        │
        ▼
[Frontend] Displays results on canvas
        │
        ▼
User reviews and clicks "Apply"
        │
        ▼
[Service] Updates plant positions in database
        │
        ▼
[Frontend] Refreshes display with saved positions
```

### Heatmap Generation Flow

```
User selects species "Tomato" and clicks "Show Heatmap"
        │
        ▼
GET /api/placement/heatmap/{forestId}?species=Tomato
        │
        ▼
[Controller] Validates forest exists
        │
        ▼
[Service] Retrieves existing plants in forest
        │
        ▼
[Heatmap Generator] For each cell (x, y):
        │   ├─ Check if occupied → score = 0.0
        │   ├─ Calculate distance to each existing plant
        │   ├─ Apply companion planting scores
        │   ├─ Apply disease spacing penalties
        │   ├─ Apply resource competition penalties
        │   └─ Normalize score to 0.0-1.0 range
        ▼
[Service] Return array of HeatmapCell objects
        │
        ▼
[Controller] Format JSON response
        │
        ▼
[Frontend] Renders color overlay on canvas
        │   • Green (score > 0.7) = optimal
        │   • Yellow (score 0.4-0.7) = acceptable
        │   • Red (score < 0.4) = poor
        ▼
User sees visual recommendations
```

---

## Genetic Algorithm Detail

### Population Structure

```
Generation 0 (Initial):
├─ Solution 1 [fitness: 5.2]
│  ├─ Plant1 @ (3, 4)
│  ├─ Plant2 @ (5, 6)
│  └─ Plant3 @ (1, 2)
├─ Solution 2 [fitness: 7.8]
│  ├─ Plant1 @ (2, 2)
│  ├─ Plant2 @ (3, 3)
│  └─ Plant3 @ (7, 8)
├─ Solution 3 [fitness: 3.1]
│  └─ ...
└─ ... (100 solutions total)

    ▼ Tournament Selection

Parent Pool:
├─ Solution 2 [fitness: 7.8] ◄─ Best from tournament
├─ Solution 47 [fitness: 6.5]
└─ ...

    ▼ Crossover (70% chance)

Offspring:
├─ Plant1 @ (2, 2) ◄─ From Parent 1
├─ Plant2 @ (8, 1) ◄─ From Parent 2
└─ Plant3 @ (3, 3) ◄─ From Parent 1

    ▼ Mutation (15% chance)

Mutated Offspring:
├─ Plant1 @ (2, 2)
├─ Plant2 @ (3, 3) ◄─ Swapped with Plant3
└─ Plant3 @ (8, 1) ◄─ Swapped with Plant2

    ▼ Repeat for 200 generations

Generation 200 (Final):
├─ Solution 1 [fitness: 18.7] ◄─ BEST SOLUTION
├─ Solution 2 [fitness: 16.3]
└─ ...
```

### Fitness Calculation Detail

```
For each pair of plants (i, j):

    distance = √[(xi - xj)² + (yi - yj)²]

    IF distance ≤ 3.0:
        compatScore = getCompatibilityScore(speciesA, speciesB)
        fitness += compatScore × (4.0 - distance)

    IF sameSpecies AND distance < 3.0:
        fitness -= (3.0 - distance) × 2.0  // Heavy penalty

    IF sameSpecies AND 3.0 ≤ distance ≤ 5.0:
        fitness += 1.0  // Bonus for good spacing

    IF distance < 2.0:
        fitness -= (2.0 - distance) × 0.5  // Resource competition

For each plant i:
    distFromEdge = min(xi, width-xi-1, yi, height-yi-1)
    fitness += distFromEdge × 0.1  // Central position bonus

RETURN fitness
```

---

## Component Interaction Diagram

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ 1. User action
       ▼
┌──────────────────┐
│  HTML/JS/Canvas  │◄──────────┐
└──────┬───────────┘           │
       │ 2. HTTP Request       │ 8. JSON Response
       ▼                       │
┌──────────────────┐           │
│   REST API       │───────────┘
│   Controller     │
└──────┬───────────┘
       │ 3. Call service
       ▼
┌───────────────────┐
│  Optimization     │
│  Service          │
└──────┬────────────┘
       │ 4. Get data
       ▼
┌───────────────────┐
│  Repository       │
│  (MongoDB)        │
└──────┬────────────┘
       │ 5. Return entities
       ▼
┌───────────────────┐
│  Genetic          │
│  Algorithm        │
│  Engine           │◄────────────┐
└──────┬────────────┘             │
       │ 6. Initialize            │ 7. Evolve
       │ population               │ 200 generations
       └──────────────────────────┘
       │
       ▼ Best solution
┌───────────────────┐
│  Format result    │
│  (PlacementSolution)
└───────────────────┘
```

---

## Technology Stack

```
┌──────────────────────────────────────────┐
│           Frontend Layer                 │
│  • HTML5 Canvas                          │
│  • Vanilla JavaScript (ES6+)            │
│  • CSS3 (Grid, Flexbox)                 │
│  • Fetch API for HTTP requests          │
└──────────────────────────────────────────┘
                    │
                    ▼ REST/JSON
┌──────────────────────────────────────────┐
│          Application Layer               │
│  • Spring Boot 3.3.3                     │
│  • Spring Web MVC                        │
│  • Spring Data MongoDB                   │
│  • Jakarta Validation                    │
│  • Swagger/OpenAPI                       │
└──────────────────────────────────────────┘
                    │
                    ▼ MongoDB Driver
┌──────────────────────────────────────────┐
│           Data Layer                     │
│  • MongoDB 6.0+                          │
│  • Collections: plants, forests, species │
│  • Indexes: species.name, plant.forestId │
└──────────────────────────────────────────┘

┌──────────────────────────────────────────┐
│          Build & Test Tools              │
│  • Gradle 9.2.0                          │
│  • JUnit 5                               │
│  • Embedded MongoDB for tests            │
│  • Docker + Docker Compose              │
└──────────────────────────────────────────┘
```

---

## File Organization

```
GreenDesk/
├── src/main/java/org/example/
│   ├── entities/placement/
│   │   ├── PlacementConstraint.java      ──┐
│   │   ├── PlacementSolution.java          │ Domain
│   │   ├── PlantPosition.java              │ Models
│   │   └── HeatmapCell.java              ──┘
│   │
│   ├── services/
│   │   └── PlacementOptimizationService.java  ─ Business Logic
│   │
│   └── controllers/placement/
│       └── PlacementOptimizationController.java  ─ REST API
│
├── src/main/resources/static/
│   └── placement-optimizer.html  ─ Frontend UI
│
├── src/test/java/org/example/
│   └── TestPlacementOptimization.java  ─ Unit Tests
│
├── scripts/
│   ├── demo-placement-optimizer.sh   ──┐
│   └── demo-placement-optimizer.bat  ──┘ Demo Scripts
│
└── docs/
    ├── PLACEMENT_OPTIMIZER.md          ──┐
    ├── PLACEMENT_OPTIMIZER_GUIDE.md      │ Documentation
    ├── IMPLEMENTATION_SUMMARY.md         │
    ├── QUICKSTART_OPTIMIZER.md           │
    └── ARCHITECTURE_OPTIMIZER.md (this)──┘
```

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────┐
│              Docker Compose Stack               │
│                                                  │
│  ┌────────────────────┐   ┌─────────────────┐  │
│  │   GreenDesk App    │   │    MongoDB      │  │
│  │   (Spring Boot)    │◄──┤   Database      │  │
│  │   Port: 8080       │   │   Port: 27017   │  │
│  └────────────────────┘   └─────────────────┘  │
│           │                                      │
│           └─────────┐                            │
│                     ▼                            │
│           ┌─────────────────┐                   │
│           │  Mongo Express  │                   │
│           │  (Web UI)       │                   │
│           │  Port: 8081     │                   │
│           └─────────────────┘                   │
└─────────────────────────────────────────────────┘
                     │
                     ▼ External Access
         ┌──────────────────────┐
         │   User's Browser     │
         │  • localhost:8080    │
         │  • localhost:8081    │
         └──────────────────────┘
```

### Launch Commands

```bash
# Development
./gradlew bootRun

# Docker
docker compose up -d

# Access points:
# - App: http://localhost:8080/placement-optimizer.html
# - API: http://localhost:8080/swagger-ui.html
# - DB:  http://localhost:8081 (Mongo Express)
```

---

## Security Considerations

```
┌─────────────────────────────────────┐
│        Security Layers              │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Input Validation            │  │
│  │  • @Valid annotations        │  │
│  │  • Size limits               │  │
│  │  • Type checking             │  │
│  └──────────────────────────────┘  │
│               ▼                     │
│  ┌──────────────────────────────┐  │
│  │  Business Logic Validation   │  │
│  │  • Forest bounds checking    │  │
│  │  • Position uniqueness       │  │
│  │  • Plant existence           │  │
│  └──────────────────────────────┘  │
│               ▼                     │
│  ┌──────────────────────────────┐  │
│  │  Database Constraints        │  │
│  │  • Unique indexes            │  │
│  │  • Foreign key refs (@DBRef) │  │
│  │  • Required fields           │  │
│  └──────────────────────────────┘  │
│               ▼                     │
│  ┌──────────────────────────────┐  │
│  │  Error Handling              │  │
│  │  • Try-catch blocks          │  │
│  │  • Custom exceptions         │  │
│  │  • User-friendly messages    │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

---

## Performance Optimization

```
Bottleneck Mitigation Strategies:

1. Algorithm Level:
   ├─ Population size tuning (100 default)
   ├─ Early termination on convergence
   └─ Parallel fitness evaluation (future)

2. Data Access:
   ├─ Batch plant retrieval
   ├─ MongoDB indexing on forestId
   └─ Caching species compatibility matrix

3. Frontend:
   ├─ Canvas rendering optimization
   ├─ Debounced user interactions
   └─ Lazy heatmap generation

4. API Response:
   ├─ Pagination for large results
   ├─ Compression (GZIP)
   └─ Selective field inclusion
```

---

## Monitoring & Observability

```
┌─────────────────────────────────────┐
│         Logging Points              │
│                                     │
│  Controller:                        │
│  ├─ Request received (INFO)         │
│  ├─ Validation errors (WARN)        │
│  └─ Response sent (INFO)            │
│                                     │
│  Service:                           │
│  ├─ Optimization started (INFO)     │
│  ├─ Generation progress (DEBUG)     │
│  ├─ Best fitness per 50 gen (INFO)  │
│  └─ Optimization completed (INFO)   │
│                                     │
│  Repository:                        │
│  ├─ Query execution (DEBUG)         │
│  └─ Data not found (WARN)           │
│                                     │
│  Metrics to track:                  │
│  ├─ Avg optimization time           │
│  ├─ Avg fitness score               │
│  ├─ Request rate                    │
│  └─ Error rate                      │
└─────────────────────────────────────┘
```

---

**This architecture supports:**
- ✅ Scalability (stateless design)
- ✅ Maintainability (clean separation)
- ✅ Testability (dependency injection)
- ✅ Extensibility (strategy pattern for fitness)
- ✅ Performance (optimized algorithms)
