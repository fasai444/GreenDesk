package org.example.entities.environment;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SeasonCycleTest {

    @Test
    void testConstructor() {
        SeasonCycle cycle = new SeasonCycle("forest-123");
        
        assertEquals("forest-123", cycle.getForestId());
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        assertEquals(0, cycle.getMonthsInCurrentSeason());
        assertEquals(3, cycle.getMonthsPerSeason());
        assertNotNull(cycle.getLastUpdate());
        assertTrue(cycle.isActive());
    }

    @Test
    void testDefaultConstructor() {
        SeasonCycle cycle = new SeasonCycle();
        assertEquals(3, cycle.getMonthsPerSeason());
    }

    @Test
    void testAdvanceTimeWithinSameSeason() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        LocalDateTime initialUpdate = cycle.getLastUpdate();
        
        cycle.advanceTime(2); // Advance 2 months, assuming 3 months per season
        
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        assertEquals(2, cycle.getMonthsInCurrentSeason());
        assertTrue(cycle.getLastUpdate().isAfter(initialUpdate) || cycle.getLastUpdate().isEqual(initialUpdate));
    }

    @Test
    void testAdvanceTimeChangesSeason() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        
        cycle.advanceTime(3); // Advance 3 months, should trigger season change
        
        assertEquals(SeasonType.SUMMER, cycle.getCurrentSeason());
        assertEquals(0, cycle.getMonthsInCurrentSeason());
    }

    @Test
    void testAdvanceTimeMultipleSeasons() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        
        cycle.advanceTime(7); // Advance 7 months (Spring -> Summer -> Autumn, +1 month into Autumn)
        
        assertEquals(SeasonType.AUTUMN, cycle.getCurrentSeason());
        assertEquals(1, cycle.getMonthsInCurrentSeason());
    }
    
    @Test
    void testAdvanceTimeFullCycle() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        
        cycle.advanceTime(12); // Advance 12 months (Full year cycle)
        
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        assertEquals(0, cycle.getMonthsInCurrentSeason());
    }

    @Test
    void testAdvanceTimeWhenInactive() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        cycle.setActive(false);
        LocalDateTime initialUpdate = cycle.getLastUpdate();
        
        cycle.advanceTime(5);
        
        // State should remain unchanged
        assertEquals(SeasonType.SPRING, cycle.getCurrentSeason());
        assertEquals(0, cycle.getMonthsInCurrentSeason());
        assertEquals(initialUpdate, cycle.getLastUpdate());
    }

    @Test
    void testGetCurrentSeasonData() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        
        Season currentSeasonData = cycle.getCurrentSeasonData();
        
        assertNotNull(currentSeasonData);
        assertEquals(SeasonType.SPRING, currentSeasonData.getType());
        assertEquals("Printemps", currentSeasonData.getName());
    }
    
    @Test
    void testSetters() {
        SeasonCycle cycle = new SeasonCycle("forest-1");
        LocalDateTime newTime = LocalDateTime.now().minusDays(1);
        
        cycle.setForestId("new-forest");
        cycle.setCurrentSeason(SeasonType.WINTER);
        cycle.setMonthsInCurrentSeason(2);
        cycle.setMonthsPerSeason(4);
        cycle.setLastUpdate(newTime);
        
        assertEquals("new-forest", cycle.getForestId());
        assertEquals(SeasonType.WINTER, cycle.getCurrentSeason());
        assertEquals(2, cycle.getMonthsInCurrentSeason());
        assertEquals(4, cycle.getMonthsPerSeason());
        assertEquals(newTime, cycle.getLastUpdate());
    }
}