package org.example.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.example.util.ParisTime;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Sérialise les LocalDateTime météo avec l'offset Europe/Paris dans le JSON API.
 */
public class ParisLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(ParisTime.formatWithOffset(value));
    }
}
