package org.example.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Heure de référence du projet : Europe/Paris (forêts et alertes en Île-de-France).
 * Les {@link LocalDateTime} météo sont interprétés comme heure murale Paris.
 */
public final class ParisTime {

    public static final ZoneId ZONE = ZoneId.of("Europe/Paris");

    private ParisTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    /**
     * Parse un timestamp webhook / API.
     * - avec Z ou offset → converti en heure Paris
     * - sans fuseau → heure murale Paris
     */
    public static LocalDateTime parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return now();
        }
        String value = raw.trim();

        try {
            if (value.endsWith("Z") || value.endsWith("z")) {
                return Instant.parse(value).atZone(ZONE).toLocalDateTime();
            }
        } catch (DateTimeParseException ignored) {
            // continue
        }

        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                    .atZoneSameInstant(ZONE)
                    .toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // continue
        }

        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            return Instant.parse(value).atZone(ZONE).toLocalDateTime();
        }
    }

    public static String formatWithOffset(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
