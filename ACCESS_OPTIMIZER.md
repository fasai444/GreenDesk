# How to Access the Plant Placement Optimizer

## Quick Access

### **Direct URL** (Fastest)
```
http://localhost:8080/placement-optimizer.html
```

---

## 📍 Access Methods

### **Method 1: Direct Browser Access**
1. Start application: `./gradlew bootRun`
2. Open browser
3. Go to: `http://localhost:8080/placement-optimizer.html`

### **Method 2: Via Navigation Menu** (NEW!)
The optimizer is now accessible from all main pages:

1. Start application
2. Open any page:
   - `http://localhost:8080/home.html`
   - `http://localhost:8080/dashboard.html`
   - `http://localhost:8080/species.html`
   - `http://localhost:8080/effects.html`
   - `http://localhost:8080/disease.html`
   - `http://localhost:8080/index.html`

3. Look for **"🧬 Optimizer"** in the top navigation bar (green text)
4. Click to access the optimizer

### **Method 3: Via Demo Script**
```bash
# Windows
scripts\demo-placement-optimizer.bat

# Linux/Mac
./scripts/demo-placement-optimizer.sh
```
Then open: `http://localhost:8080/placement-optimizer.html`

---

## 📂 File Locations

### **Frontend UI**
```
GreenDesk/src/main/resources/static/placement-optimizer.html
```

### **Backend Code**
```
GreenDesk/src/main/java/org/example/
├── controllers/placement/PlacementOptimizationController.java
├── services/PlacementOptimizationService.java
└── entities/placement/
    ├── PlacementConstraint.java
    ├── PlacementSolution.java
    ├── PlantPosition.java
    └── HeatmapCell.java
```

### **Tests**
```
GreenDesk/src/test/java/org/example/TestPlacementOptimization.java
```

---

## 🔗 All Available URLs

Once application is running (`./gradlew bootRun`):

| Page | URL |
|------|-----|
| **Optimizer** (NEW) | http://localhost:8080/placement-optimizer.html |
| Home | http://localhost:8080/home.html |
| Dashboard | http://localhost:8080/dashboard.html |
| Simulation | http://localhost:8080/index.html |
| Species | http://localhost:8080/species.html |
| Effects | http://localhost:8080/effects.html |
| Disease Analysis | http://localhost:8080/disease.html |
| API Docs | http://localhost:8080/swagger-ui.html |

---

## 🧪 API Endpoints

### Test via curl or Postman:

```bash
# Optimize placement
curl -X POST http://localhost:8080/api/placement/optimize \
  -H "Content-Type: application/json" \
  -d '{"plantIds":["id1","id2"],"forestWidth":10,"forestHeight":10}'

# Get heatmap
curl "http://localhost:8080/api/placement/heatmap/FOREST_ID?species=Tomato"

# Get suggestion
curl "http://localhost:8080/api/placement/suggest/FOREST_ID?species=Basil"

# Apply optimization
curl -X POST http://localhost:8080/api/placement/optimize-and-apply/FOREST_ID \
  -H "Content-Type: application/json" \
  -d '["plant1_id","plant2_id"]'
```

---

## 🎯 Visual Guide

### Navigation Bar Location:
```
┌─────────────────────────────────────────────────────────┐
│ 🌿 GreenDesk    Home  Dashboard  Species  [🧬 Optimizer]│
└─────────────────────────────────────────────────────────┘
                                               ▲
                                    Click here to access!
```

### What You'll See:
```
┌───────────────────────────────────────────────────────────┐
│        🌱 Plant Placement Optimizer                       │
│    AI-powered optimal plant placement using genetic       │
│                    algorithms                             │
├───────────────┬───────────────────────────────────────────┤
│ Control Panel │           Forest Canvas                   │
│               │                                           │
│ [Forest ▼]    │   ┌─────────────────────────────┐       │
│               │   │  [Grid with plants]         │       │
│ ☐ Plant 1     │   │  🔵 🟢 🟠 (colored circles) │       │
│ ☐ Plant 2     │   │  Heatmap overlay (optional) │       │
│ ☐ Plant 3     │   └─────────────────────────────┘       │
│               │                                           │
│ [🧬 Optimize] │   Fitness Score: 15.67                   │
│ [✓ Apply]     │   Plants Placed: 10                      │
│               │                                           │
│ Species: [▼]  │   Legend:                                │
│ [🔥 Heatmap]  │   🔵 Tomato  🟢 Basil  🟠 Carrot        │
└───────────────┴───────────────────────────────────────────┘
```

---

## ✅ Verification Steps

### 1. Check Application is Running
```bash
curl http://localhost:8080/api/species
```
Should return list of species (JSON)

### 2. Check Optimizer Page Loads
Open browser → `http://localhost:8080/placement-optimizer.html`
- Should see "Plant Placement Optimizer" header
- Should see forest selection dropdown
- Should see canvas grid

### 3. Check API is Available
```bash
curl http://localhost:8080/swagger-ui.html
```
Look for "placement-optimization-controller" section

---

## 🐛 Troubleshooting

### "Page Not Found (404)"
→ Application not running. Start with: `./gradlew bootRun`

### "Connection Refused"
→ Check port 8080 is not in use:
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### "No forests/plants shown"
→ Run demo script to create test data:
```bash
scripts\demo-placement-optimizer.bat
```

### Navigation link not appearing
→ Rebuild and restart:
```bash
./gradlew clean build
./gradlew bootRun
```

---

## 📚 Documentation

After accessing the page, read:
- **Quick Start**: [QUICKSTART_OPTIMIZER.md](QUICKSTART_OPTIMIZER.md)
- **Complete Guide**: [PLACEMENT_OPTIMIZER_GUIDE.md](PLACEMENT_OPTIMIZER_GUIDE.md)
- **API Reference**: [docs/PLACEMENT_OPTIMIZER.md](docs/PLACEMENT_OPTIMIZER.md)
- **Architecture**: [ARCHITECTURE_OPTIMIZER.md](ARCHITECTURE_OPTIMIZER.md)

---

## 🎓 First-Time Users

Follow this sequence:

1. **Start app**: `./gradlew bootRun`
2. **Run demo**: `scripts\demo-placement-optimizer.bat`
3. **Open browser**: `http://localhost:8080/placement-optimizer.html`
4. **Select forest**: "Optimization Demo Forest"
5. **Check plants**: Select 3-5 plants
6. **Click optimize**: Wait 10 seconds
7. **View results**: See fitness score and placement
8. **Try heatmap**: Select species and click "Show"
9. **Apply**: Click "Apply to Forest" to save

---

**You're ready!** The optimizer is fully integrated and accessible from anywhere in your application. 🚀
