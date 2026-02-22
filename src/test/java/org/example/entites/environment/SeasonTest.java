package org.example.entites.environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeasonTest {

    @Test
    void testConstructorAndGetters() {
        Season customSeason = new Season(SeasonType.SUMMER, "Custom Summer", 10.0, -5.0, 2000.0, -10.0);
        
        assertEquals(SeasonType.SUMMER, customSeason.getType());
        assertEquals("Custom Summer", customSeason.getName());
        assertEquals(10.0, customSeason.getTemperatureModifier());
        assertEquals(-5.0, customSeason.getHumidityModifier());
        assertEquals(2000.0, customSeason.getLuxModifier());
        assertEquals(-10.0, customSeason.getRainfallModifier());
    }

    @Test
    void testSetters() {
        Season season = Season.getSpring();
        
        season.setType(SeasonType.AUTUMN);
        season.setName("Late Autumn");
        season.setTemperatureModifier(-2.0);
        season.setHumidityModifier(15.0);
        season.setLuxModifier(-500.0);
        season.setRainfallModifier(40.0);
        
        assertEquals(SeasonType.AUTUMN, season.getType());
        assertEquals("Late Autumn", season.getName());
        assertEquals(-2.0, season.getTemperatureModifier());
        assertEquals(15.0, season.getHumidityModifier());
        assertEquals(-500.0, season.getLuxModifier());
        assertEquals(40.0, season.getRainfallModifier());
    }

    @Test
    void testGetWinter() {
        Season winter = Season.getWinter();
        assertEquals(SeasonType.WINTER, winter.getType());
        assertEquals("Hiver", winter.getName());
        assertEquals(-8.0, winter.getTemperatureModifier());
    }

    @Test
    void testGetSpring() {
        Season spring = Season.getSpring();
        assertEquals(SeasonType.SPRING, spring.getType());
        assertEquals("Printemps", spring.getName());
        assertEquals(5.0, spring.getTemperatureModifier());
    }

    @Test
    void testGetSummer() {
        Season summer = Season.getSummer();
        assertEquals(SeasonType.SUMMER, summer.getType());
        assertEquals("Été", summer.getName());
        assertEquals(12.0, summer.getTemperatureModifier());
    }

    @Test
    void testGetAutumn() {
        Season autumn = Season.getAutumn();
        assertEquals(SeasonType.AUTUMN, autumn.getType());
        assertEquals("Automne", autumn.getName());
        assertEquals(0.0, autumn.getTemperatureModifier());
    }

    @Test
    void testGetByType() {
        assertEquals(SeasonType.WINTER, Season.getByType(SeasonType.WINTER).getType());
        assertEquals(SeasonType.SPRING, Season.getByType(SeasonType.SPRING).getType());
        assertEquals(SeasonType.SUMMER, Season.getByType(SeasonType.SUMMER).getType());
        assertEquals(SeasonType.AUTUMN, Season.getByType(SeasonType.AUTUMN).getType());
    }
}