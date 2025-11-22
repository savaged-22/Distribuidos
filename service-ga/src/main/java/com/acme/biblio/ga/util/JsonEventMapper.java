package com.acme.biblio.ga.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.acme.biblio.contracts.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JsonEventMapper {

    private static final Logger log = LoggerFactory.getLogger(JsonEventMapper.class);

    // Mapper global y seguro para serializar/deserializar
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonEventMapper() {}

    /**
     * Convierte un mensaje (Evento/Response/Command) a JSON para guardarlo en GA_OUTBOX.
     */
    public static String toJson(Message msg) {
        try {
            return mapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            log.error("❌ Error serializando evento: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo serializar evento a JSON", e);
        }
    }

    /**
     * Convierte JSON almacenado en GA_OUTBOX de vuelta a Message (polimórfico).
     */
    public static <T extends Message> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("❌ Error deserializando evento: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo deserializar evento", e);
        }
    }
}
