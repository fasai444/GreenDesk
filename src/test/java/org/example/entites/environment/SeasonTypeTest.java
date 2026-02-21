package org.example.entites.environment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeasonTypeTest {

    @Test
    void testNextSeasonSequence() {
        assertEquals(SeasonType.SPRING, SeasonType.WINTER.next());
        assertEquals(SeasonType.SUMMER, SeasonType.SPRING.next());
        assertEquals(SeasonType.AUTUMN, SeasonType.SUMMER.next());
        assertEquals(SeasonType.WINTER, SeasonType.AUTUMN.next());
    }
}