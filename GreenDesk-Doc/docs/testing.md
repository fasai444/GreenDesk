# Guide des tests

Stratégie de test et guide pour exécuter les tests du projet.

## Vue d'ensemble

GreenDesk utilise **JUnit 5** pour les tests unitaires et d'intégration.

```
Tests
├── Unitaires
│   ├── SpeciesServiceTest
│   ├── PlantServiceTest
│   ├── ForestServiceTest
│   └── EffectServiceTest
├── Intégration
│   ├── SpeciesControllerTest
│   ├── PlantControllerTest
│   └── ForestControllerTest
└── Scénarios
    ├── TestPlantLifecycle
    ├── TestForestAndSeasons
    └── TestEcosystemServices
```

## Exécuter les tests

### Tous les tests

```bash
./gradlew test
```

### Tests spécifiques

```bash
# Une classe
./gradlew test --tests SpeciesServiceTest

# Une méthode
./gradlew test --tests SpeciesServiceTest.testCreateSpecies

# Pattern
./gradlew test --tests "*Service*"
```

### Avec rapport

```bash
./gradlew test jacocoTestReport

# Consultez le rapport
# build/reports/tests/test/index.html
# build/reports/jacoco/test/html/index.html
```

## Structure des tests

### Tests unitaires

Testent services isolément sans dépendances externes :

```java
@SpringBootTest
public class SpeciesServiceTest {
    
    @MockBean
    private SpeciesRepository repository;
    
    @Autowired
    private SpeciesService service;
    
    @Test
    public void testCreateSpecies() {
        // Arrange
        Species species = new Species("Rose", 500.0, ...);
        when(repository.save(any())).thenReturn(species);
        
        // Act
        Species result = service.createSpecies(species);
        
        // Assert
        assertNotNull(result.getId());
        assertEquals("Rose", result.getName());
    }
}
```

### Tests d'intégration

Testent la chaîne complète avec MongoDB réel :

```java
@SpringBootTest
@AutoConfigureMockMvc
public class PlantControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Test
    public void testCreatePlant() throws Exception {
        mockMvc.perform(post("/api/plants/Rose")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"Ma Rose\", ...}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Ma Rose"));
    }
}
```

### Tests de scénarios

Testent des workflows complets :

```java
@SpringBootTest
public class TestPlantLifecycle {
    
    @Test
    public void testCompletePlantLifecycle() {
        // 1. Créer espèce
        Species rose = createSpecies("Rose", ...);
        
        // 2. Créer plante
        Plant plant = createPlant("Ma Rose", rose);
        
        // 3. Vérifier santé initiale
        assertEquals(PlantStatus.HEALTHY, plant.getStatus());
        
        // 4. Modifier environnement
        plant.getEnvironment().setWater(100);
        
        // 5. Vérifier stress
        assertEquals(PlantStatus.STRESSED, recalculateStatus(plant));
        
        // 6. Appliquer effet
        addEffect(plant, Effect.EXTRA_WATERING);
        
        // 7. Vérifier amélioration
        assertEquals(PlantStatus.HEALTHY, recalculateStatus(plant));
    }
}
```

## Tests disponibles

### Espèces (SpeciesServiceTest)

- Créer espèce avec données valides
- Rejeter espèce sans nom
- Rejeter valeurs négatives
- Récupérer par nom
- Mettre à jour espèce
- Supprimer espèce

### Plantes (PlantServiceTest)

- Créer plante d'une espèce
- Calculer santé correctement
- Détecter stress
- Appliquer effets
- Retirer effets
- Calculer croissance

### Forêts (ForestServiceTest)

- Créer forêt
- Ajouter plante à position
- Rejeter position occupée
- Valider variation génétique
- Progresser saisons
- Appliquer modificateurs saisonniers

### Effets (EffectServiceTest)

- Appliquer SHADE (-30% lumière)
- Appliquer FERTILIZER (+20% croissance)
- Appliquer EXTRA_WATERING (+15% eau)
- Appliquer HEATING (+3°C)
- Combiner plusieurs effets
- Retirer effets

### Scénarios complets

- **TestPlantLifecycle** - Cycle de vie plant
- **TestForestAndSeasons** - Transitions saisonnières
- **TestEcosystemServices** - Services d'écosystème
- **TestPlantServices** - Services plante
- **TestSensorReadingsAndAlerts** - Lectures/alertes
- **TestEffects** - Système d'effets

## Cas de test importants

### Santé critique

```java
@Test
public void testPlantBecomingDiseased() {
    Plant plant = createPlant("Rose", optimal_conditions);
    assertEquals(PlantStatus.HEALTHY, plant.getStatus());
    
    // Stress extrême
    plant.getEnvironment().setWater(10);  // vs 500 optimal
    plant.getEnvironment().setTemperature(40); // vs 20 optimal
    
    double health = calculateHealth(plant);
    assertTrue(health < 20);
    assertEquals(PlantStatus.DISEASED, plant.getStatus());
}
```

### Variation génétique

```java
@Test
public void testPlantVariationAffectsGrowth() {
    Plant plant1 = createPlant(species, variationSeed=0.8);
    Plant plant2 = createPlant(species, variationSeed=1.2);
    
    double growth1 = calculateGrowth(plant1);
    double growth2 = calculateGrowth(plant2);
    
    assertTrue(growth2 > growth1);  // 1.2 > 0.8
}
```

### Transitions saisonnières

```java
@Test
public void testSeasonalTransition() {
    Forest forest = createForest();
    assertEquals(Season.SPRING, forest.getCurrentSeason());
    
    progressSeason(forest);
    assertEquals(Season.SUMMER, forest.getCurrentSeason());
    
    // Vérifier le modificateur
    SeasonModifier mod = getModifier(Season.SUMMER);
    assertEquals(1.2, mod.getWaterMultiplier());
    assertEquals(5.0, mod.getTemperatureModifier());
}
```

## Métriques de test

### Coverage (JaCoCo)

```bash
./gradlew jacocoTestReport
```

**Rapport** :
```
Overall  : 85%
Services : 92%
Controllers : 88%
Entities : 70%
Repositories : 60% (peu de logique)
```

**Objectif** : > 80% pour services/controllers

### Critères d'acceptation

- Tous les happy paths testés
- Les erreurs main traitées
- Les validations vérifiées
- Les edge cases couverts

## Bonnes pratiques

### À faire

```java
// Noms descriptifs
@Test
public void shouldReturnHealthyWhenEnvironmentIsOptimal() { }

// Arrange-Act-Assert
@Test
public void testPlantStress() {
    // Arrange
    Plant plant = createPlant(...);
    
    // Act
    double stress = calculateStress(plant);
    
    // Assert
    assertEquals(0.0, stress);
}

// Tester une chose
@Test
public void testOnlyWaterStress() {
    // Tester seulement water, pas temp, lux, etc.
}
```

### À éviter

```java
// Noms vagues
@Test
public void test1() { }

// Multiple assertions sans lien
@Test
public void testEverything() {
    assertTrue(a);
    assertTrue(b);
    assertTrue(c);
}

// Pas d'isolation
@Test
public void testDependentOnOtherTest() {
    // Dépend de résultat d'autre test
}
```

## Debugging tests

### Afficher logs
```bash
./gradlew test -i
```

### Test unique
```bash
./gradlew test --tests TestPlantLifecycle.testCompletePlantLifecycle -info
```

### Debug breakpoint (IDE)

Pour déboguer dans votre IDE :
1. Placez breakpoint dans test
2. Run → Debug
3. Inspectez variables

## Continuous Integration

Tests exécutés automatiquement :

```bash
# GitHub Actions / CI Pipeline
git push
→ ./gradlew test
→ ./gradlew jacocoTestReport
→ Rapport commenté sur PR
```

## Performance des tests

### Temps d'exécution

```
Total : ~10 secondes
- Unitaires : 5 sec
- Intégration : 4 sec
- Scénarios : 1 sec
```

### Optimisation

- Tests parallèles : `gradle.properties`
- Exclure tests slow : `@IgnoreOn`
- MockMvc pour éviter démarrage complet

## Futur

- Augmenter coverage à 90%
- Tests de performance
- Tests d'acceptance (Selenium)
- Benchmarks

---

**Prêt à contribuer ?** Consultez [Guide de contribution](contributing.md) !
