package org.example.services;

import org.example.entities.environment.Season;
import org.example.entities.environment.SeasonCycle;
import org.example.repositories.SeasonCycleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeasonServiceTest {

    @Mock
    private SeasonCycleRepository seasonCycleRepository;

    @InjectMocks
    private SeasonService seasonService;

    @Test
    void getAllSeasons_shouldReturnCatalog() {
        assertEquals(4, seasonService.getAllSeasons().size());
    }

    @Test
    void create_get_advance_toggle_delete_cycle() throws Exception {
        SeasonCycle cycle = new SeasonCycle("forest-1");

        when(seasonCycleRepository.findByForestId("forest-1")).thenReturn(Optional.empty(), Optional.of(cycle));
        when(seasonCycleRepository.save(any(SeasonCycle.class))).thenAnswer(inv -> inv.getArgument(0));

        SeasonCycle created = seasonService.createSeasonCycle("forest-1");
        assertEquals("forest-1", created.getForestId());

        assertTrue(seasonService.getSeasonCycle("forest-1").isPresent());

        SeasonCycle advanced = seasonService.advanceSeasonCycle("forest-1", 3);
        assertNotNull(advanced);

        Season current = seasonService.getCurrentSeason("forest-1");
        assertNotNull(current);

        SeasonCycle toggled = seasonService.toggleSeasonCycle("forest-1", false);
        assertFalse(toggled.isActive());

        seasonService.deleteSeasonCycle("forest-1");
        verify(seasonCycleRepository, atLeastOnce()).delete(any(SeasonCycle.class));
    }

    @Test
    void methods_shouldThrow_whenCycleMissing() {
        when(seasonCycleRepository.findByForestId("missing")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> seasonService.advanceSeasonCycle("missing", 1));
        assertThrows(Exception.class, () -> seasonService.getCurrentSeason("missing"));
        assertThrows(Exception.class, () -> seasonService.toggleSeasonCycle("missing", true));
    }
}
