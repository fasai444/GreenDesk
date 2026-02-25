package org.example.entities.species;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Species – tests prioritaires")
class SpeciesTest {

    private final Species tomato = new Species(
            "Tomate", 220, 24, 65, 3000, 1.5, 0.4
    );

    @Test
    void full_constructor_stores_values_correctly() {
        assertThat(tomato.getName()).isEqualTo("Tomate");
        assertThat(tomato.getOptimalWaterNeeds()).isEqualTo(220);
        assertThat(tomato.getBaseGrowthRate()).isEqualTo(1.5);
    }

    @Test
    void random_constructor_generates_plausible_values() {
        Species s = new Species("Test");
        assertThat(s.getOptimalTemperature()).isBetween(15.0, 30.0);
    }

    @ParameterizedTest(name = "Eau {0} → optimal = {1}")
    @CsvSource({
            "210, true",
            "235, true",
            "190, false"
    })
    void isOptimalWaterNeeds(double value, boolean expected) {
        assertThat(tomato.isOptimalWaterNeeds(value)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Temp {0} → optimal = {1}")
    @CsvSource({
            "22, true",
            "28, true",
            "18, false"
    })
    void isOptimalTemperature(double value, boolean expected) {
        assertThat(tomato.isOptimalTemperature(value)).isEqualTo(expected);
    }

    @Test
    void tempStressFactor_zero_in_tolerance() {
        assertThat(tomato.tempStressFactor(24.0)).isZero();
        assertThat(tomato.tempStressFactor(18.0)).isEqualTo(0.0833, within(0.001));
    }

    @Test
    void humidityStressFactor_zero_in_tolerance() {
        assertThat(tomato.humidityStressFactor(65)).isZero();
        assertThat(tomato.humidityStressFactor(80)).isEqualTo(0.0769, within(0.001));
    }

    @Test
    void lightStressFactor_only_when_below_optimal() {
        assertThat(tomato.lightStressFactor(3000)).isEqualTo(0.0);
        assertThat(tomato.lightStressFactor(1500)).isEqualTo(0.5);
        assertThat(tomato.lightStressFactor(4000)).isEqualTo(0.0);
    }

    @Test
    void toString_contains_name_and_temp() {
        String s = tomato.toString();
        assertThat(s).contains("Tomate");
        assertThat(s).contains("temp=24.0");
    }
}
