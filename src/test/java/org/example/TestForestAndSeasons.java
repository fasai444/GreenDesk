package org.example;

import java.util.List;

import org.example.entites.forest.Forest;
import org.example.entites.plant.Plant;
import org.example.entites.environment.Season;
import org.example.entites.environment.SeasonCycle;
import org.example.entites.environment.SeasonType;
import org.example.entites.species.Species;
import org.example.repositories.ForestRepository;
import org.example.repositories.PlantRepository;
import org.example.repositories.SeasonCycleRepository;
import org.example.repositories.SpeciesRepository;
import org.example.services.ForestService;
import org.example.services.PlantServices;
import org.example.services.SeasonService;
import org.example.services.SpeciesServices;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests pour la Livraison 2 - Feature 1 : Forêts + Saisons
 * 
 * Tests des critères d'acceptation :
 * - Création de forêts A et B (10x10)
 * - Placement de 2 plantes dans chaque forêt
 * - Récupération des plantes par forêt
 * - R1 : Impossibilité d'ajouter 2 plantes sur la même position → 409
 * - R2 : 2 manguiers autorisés si variationSeed différent
 * - Saisons : variations observables
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestForestAndSeasons {
    
    @Autowired
    private ForestService forestService;
    
    @Autowired
    private PlantServices plantServices;
    
    @Autowired
    private SpeciesServices speciesServices;
    
    @Autowired
    private SeasonService seasonService;
    
    @Autowired
    private ForestRepository forestRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private SpeciesRepository speciesRepository;
    
    @Autowired
    private SeasonCycleRepository seasonCycleRepository;
    
    private Species manguierSpecies;
    private Forest forestA;
    private Forest forestB;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Nettoyer la base avant chaque test
        seasonCycleRepository.deleteAll();
        forestRepository.deleteAll();
        plantRepository.deleteAll();
        speciesRepository.deleteAll();
        
        // Créer une espèce de test (manguier)
        Species newSpecies = new Species("Manguier", 300.0, 25.0, 70.0, 5000.0, 0.5, 0.6);
        manguierSpecies = speciesServices.createSpecies(newSpecies);
    }
    
    @org.junit.jupiter.api.Test
    @Order(1)
    @DisplayName("Critère 1 : Créer forêt A (10x10) et forêt B (10x10)")
    public void testCreateForests() {
        // Créer forêt A
        forestA = forestService.createForest("Forêt A", 10, 10);
        assertNotNull(forestA.getId());
        assertEquals("Forêt A", forestA.getName());
        assertEquals(10, forestA.getWidth());
        assertEquals(10, forestA.getHeight());
        
        // Créer forêt B
        forestB = forestService.createForest("Forêt B", 10, 10);
        assertNotNull(forestB.getId());
        assertEquals("Forêt B", forestB.getName());
        assertEquals(10, forestB.getWidth());
        assertEquals(10, forestB.getHeight());
        
        // Vérifier qu'elles sont bien enregistrées
        List<Forest> forests = forestService.getAllForests();
        assertEquals(2, forests.size());
        
        System.out.println("✅ Forêt A et Forêt B créées avec succès (10x10 chacune)");
    }
    
    @org.junit.jupiter.api.Test
    @Order(2)
    @DisplayName("Critère 2 : Placer 2 plantes dans chaque forêt")
    public void testAddPlantsToForests() throws Exception {
        // Créer les forêts
        forestA = forestService.createForest("Forêt A", 10, 10);
        forestB = forestService.createForest("Forêt B", 10, 10);
        
        // Créer 4 plantes
        Plant plant1 = plantServices.createPlant("Manguier A1", manguierSpecies.getId());
        Plant plant2 = plantServices.createPlant("Manguier A2", manguierSpecies.getId());
        Plant plant3 = plantServices.createPlant("Manguier B1", manguierSpecies.getId());
        Plant plant4 = plantServices.createPlant("Manguier B2", manguierSpecies.getId());
        
        // Ajouter 2 plantes dans forêt A
        forestService.addPlantToForest(forestA.getId(), plant1.getId(), 2, 3);
        forestService.addPlantToForest(forestA.getId(), plant2.getId(), 5, 7);
        
        // Ajouter 2 plantes dans forêt B
        forestService.addPlantToForest(forestB.getId(), plant3.getId(), 1, 1);
        forestService.addPlantToForest(forestB.getId(), plant4.getId(), 8, 9);
        
        // Vérifier que les plantes sont bien ajoutées
        List<Plant> plantsA = forestService.getPlantsInForest(forestA.getId());
        List<Plant> plantsB = forestService.getPlantsInForest(forestB.getId());
        
        assertEquals(2, plantsA.size());
        assertEquals(2, plantsB.size());
        
        System.out.println("✅ 2 plantes placées dans Forêt A et 2 plantes dans Forêt B");
    }
    
    @org.junit.jupiter.api.Test
    @Order(3)
    @DisplayName("Critère 3 : Récupération des plantes par forêt")
    public void testGetPlantsInForest() throws Exception {
        // Créer forêt et plantes
        forestA = forestService.createForest("Forêt A", 10, 10);
        Plant plant1 = plantServices.createPlant("Manguier 1", manguierSpecies.getId());
        Plant plant2 = plantServices.createPlant("Manguier 2", manguierSpecies.getId());
        
        forestService.addPlantToForest(forestA.getId(), plant1.getId(), 0, 0);
        forestService.addPlantToForest(forestA.getId(), plant2.getId(), 5, 5);
        
        // Récupérer les plantes
        List<Plant> plants = forestService.getPlantsInForest(forestA.getId());
        
        assertEquals(2, plants.size());
        assertTrue(plants.stream().anyMatch(p -> p.getId().equals(plant1.getId())));
        assertTrue(plants.stream().anyMatch(p -> p.getId().equals(plant2.getId())));
        
        System.out.println("✅ Récupération des plantes par forêt fonctionne correctement");
    }
    
    @org.junit.jupiter.api.Test
    @Order(4)
    @DisplayName("R1 : Impossible d'ajouter 2 plantes sur la même position (409 Conflict)")
    public void testPositionUniqueness() throws Exception {
        // Créer forêt et plantes
        forestA = forestService.createForest("Forêt Test R1", 10, 10);
        Plant plant1 = plantServices.createPlant("Plante 1", manguierSpecies.getId());
        Plant plant2 = plantServices.createPlant("Plante 2", manguierSpecies.getId());
        
        // Ajouter première plante à (3, 5)
        forestService.addPlantToForest(forestA.getId(), plant1.getId(), 3, 5);
        
        // Tenter d'ajouter une deuxième plante à (3, 5) → doit échouer
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            forestService.addPlantToForest(forestA.getId(), plant2.getId(), 3, 5);
        });
        
        assertTrue(exception.getMessage().contains("Position (3,5) déjà occupée"));
        
        System.out.println("✅ R1 validé : Impossible d'ajouter 2 plantes à la même position");
        System.out.println("   Message d'erreur : " + exception.getMessage());
    }
    
    @org.junit.jupiter.api.Test
    @Order(5)
    @DisplayName("R2 : 2 manguiers autorisés si variationSeed différent")
    public void testPlantDiversity() throws Exception {
        // Créer forêt
        forestA = forestService.createForest("Forêt Test R2", 10, 10);
        
        // Créer 2 manguiers (même espèce)
        Plant manguier1 = plantServices.createPlant("Manguier 1", manguierSpecies.getId());
        Plant manguier2 = plantServices.createPlant("Manguier 2", manguierSpecies.getId());
        
        // Vérifier que les variationSeeds sont différents
        assertNotEquals(manguier1.getVariationSeed(), manguier2.getVariationSeed(),
            "Les plantes doivent avoir des variationSeeds différents");
        
        // Ajouter les 2 manguiers → doit réussir car variationSeeds différents
        forestService.addPlantToForest(forestA.getId(), manguier1.getId(), 2, 2);
        forestService.addPlantToForest(forestA.getId(), manguier2.getId(), 4, 4);
        
        List<Plant> plants = forestService.getPlantsInForest(forestA.getId());
        assertEquals(2, plants.size());
        
        System.out.println("✅ R2 validé : 2 manguiers autorisés avec variationSeeds différents");
        System.out.println("   Manguier 1 variationSeed : " + manguier1.getVariationSeed());
        System.out.println("   Manguier 2 variationSeed : " + manguier2.getVariationSeed());
    }
    
    @org.junit.jupiter.api.Test
    @Order(6)
    @DisplayName("Saisons : Catalogue des 4 saisons disponible")
    public void testSeasonsCatalog() {
        List<Season> seasons = seasonService.getAllSeasons();
        
        assertEquals(4, seasons.size());
        
        // Vérifier que les 4 saisons sont présentes
        assertTrue(seasons.stream().anyMatch(s -> s.getType() == SeasonType.WINTER));
        assertTrue(seasons.stream().anyMatch(s -> s.getType() == SeasonType.SPRING));
        assertTrue(seasons.stream().anyMatch(s -> s.getType() == SeasonType.SUMMER));
        assertTrue(seasons.stream().anyMatch(s -> s.getType() == SeasonType.AUTUMN));
        
        System.out.println("✅ Catalogue des 4 saisons disponible");
        seasons.forEach(s -> 
            System.out.println("   - " + s.getName() + " : temp=" + s.getTemperatureModifier() + 
                             ", lux=" + s.getLuxModifier())
        );
    }
    
    @org.junit.jupiter.api.Test
    @Order(7)
    @DisplayName("Saisons : Variations observables entre saisons")
    public void testSeasonVariations() {
        Season winter = Season.getWinter();
        Season spring = Season.getSpring();
        Season summer = Season.getSummer();
        Season autumn = Season.getAutumn();
        
        // Vérifier que les modifications sont distinctes
        assertNotEquals(winter.getTemperatureModifier(), summer.getTemperatureModifier());
        assertNotEquals(winter.getLuxModifier(), summer.getLuxModifier());
        
        // Vérifier que l'été est plus chaud que l'hiver
        assertTrue(summer.getTemperatureModifier() > winter.getTemperatureModifier());
        
        // Vérifier que l'été a plus de lumière que l'hiver
        assertTrue(summer.getLuxModifier() > winter.getLuxModifier());
        
        System.out.println("✅ Variations entre saisons clairement observables");
        System.out.println("   Hiver : temp=" + winter.getTemperatureModifier() + "°C, lux=" + winter.getLuxModifier());
        System.out.println("   Été   : temp=" + summer.getTemperatureModifier() + "°C, lux=" + summer.getLuxModifier());
    }
    
    @org.junit.jupiter.api.Test
    @Order(8)
    @DisplayName("Saisons : Cycle de saisons pour une forêt")
    public void testSeasonCycle() throws Exception {
        // Créer une forêt
        forestA = forestService.createForest("Forêt Saisonnière", 10, 10);
        
        // Créer un cycle de saisons
        SeasonCycle cycle = seasonService.createSeasonCycle(forestA.getId());
        
        assertNotNull(cycle.getId());
        assertEquals(forestA.getId(), cycle.getForestId());
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        
        // Faire avancer de 3 mois (fin du printemps)
        seasonService.advanceSeasonCycle(forestA.getId(), 3);
        cycle = seasonService.getSeasonCycle(forestA.getId()).orElseThrow();
        assertEquals(SeasonType.SUMMER, cycle.getCurrentSeason());
        
        // Faire avancer de 3 mois (fin de l'été)
        seasonService.advanceSeasonCycle(forestA.getId(), 3);
        cycle = seasonService.getSeasonCycle(forestA.getId()).orElseThrow();
        assertEquals(SeasonType.AUTUMN, cycle.getCurrentSeason());
        
        // Faire avancer de 3 mois (fin de l'automne)
        seasonService.advanceSeasonCycle(forestA.getId(), 3);
        cycle = seasonService.getSeasonCycle(forestA.getId()).orElseThrow();
        assertEquals(SeasonType.WINTER, cycle.getCurrentSeason());
        
        // Faire avancer de 3 mois (retour au printemps)
        seasonService.advanceSeasonCycle(forestA.getId(), 3);
        cycle = seasonService.getSeasonCycle(forestA.getId()).orElseThrow();
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        
        System.out.println("✅ Cycle des saisons fonctionne : printemps → été → automne → hiver → printemps");
    }
}
