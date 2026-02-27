# Quick Start - Plant Placement Optimizer

## 5-Minute Setup

### Step 1: Start Application (30 seconds)
```bash
cd GreenDesk
./gradlew bootRun
```

Wait for: `Started GreenDesk in X seconds`

### Step 2: Run Demo Script (2 minutes)

**Windows:**
```bash
scripts\demo-placement-optimizer.bat
```

**Linux/Mac:**
```bash
chmod +x scripts/demo-placement-optimizer.sh
./scripts/demo-placement-optimizer.sh
```

This creates:
- ✅ 3 species (Tomato, Basil, Carrot)
- ✅ 1 forest (12x12)
- ✅ 11 plants
- ✅ Runs optimization
- ✅ Shows results

### Step 3: Open Web UI (1 minute)

Browser: **http://localhost:8080/placement-optimizer.html**

1. Select "Optimization Demo Forest"
2. Check some plants
3. Click "🧬 Run Genetic Algorithm"
4. Wait 10 seconds
5. See optimized placement!

### Step 4: Try Heatmap (30 seconds)

1. Select species: "Tomato"
2. Click "🔥 Show"
3. Green = good placement
4. Red = bad placement

---

## What You'll See

### Fitness Score
- **20+**: Excellent arrangement
- **10-20**: Good arrangement
- **5-10**: Acceptable
- **< 5**: Poor

### Color Legend
- 🔵 Blue = Tomato
- 🟢 Green = Basil
- 🟠 Orange = Carrot

### Canvas Features
- Solid circles = Existing plants
- Dashed boxes = Proposed placement
- Colored grid = Heatmap overlay

---

## Next Steps

### Try These:
1. **Create more plants** and re-optimize
2. **Mix species** to see companion benefits
3. **Generate heatmaps** for each species
4. **Apply optimization** to save placement

### Read Documentation:
- **Tutorial**: [PLACEMENT_OPTIMIZER_GUIDE.md](PLACEMENT_OPTIMIZER_GUIDE.md)
- **API Docs**: [docs/PLACEMENT_OPTIMIZER.md](docs/PLACEMENT_OPTIMIZER.md)
- **Technical**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)

---

## Troubleshooting

### "Connection refused"
→ Make sure application is running: `./gradlew bootRun`

### "No forests found"
→ Run demo script first: `scripts/demo-placement-optimizer.bat`

### Optimization taking too long
→ Normal for first run (10-15 seconds)

### Low fitness score
→ Try different plant combinations or larger forest

---

## Manual API Test (Optional)

```bash
# Check application is running
curl http://localhost:8080/api/species

# Get suggestion for placement
curl "http://localhost:8080/api/placement/suggest/FOREST_ID?species=Tomato"
```

---

## Video Tutorial (Recommended)

1. Start app → Wait for startup
2. Run demo → Watch script output
3. Open browser → See visualization
4. Click optimize → Watch algorithm work
5. View heatmap → See recommendations
6. Apply placement → Save to database

**Total time**: 5 minutes

---

**Happy Optimizing! 🌿**

For help: See [PLACEMENT_OPTIMIZER_GUIDE.md](PLACEMENT_OPTIMIZER_GUIDE.md)
