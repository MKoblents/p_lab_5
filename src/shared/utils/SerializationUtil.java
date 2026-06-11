package shared.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.IOException;

public class SerializationUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 1. Поддержка Java 8 Date/Time
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ 2. ИСПРАВЛЕНИЕ: Включаем default typing, чтобы Джексон знал точный класс
        // при десериализации в Object (исправляет HandshakeRequest, CommandRequest и т.д.)
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING, // Используем EVERYTHING, чтобы покрыть final-классы, такие как Records
                JsonTypeInfo.As.PROPERTY
        );
    }

    public static byte[] serialize(Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    public static Object deserialize(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, Object.class);
    }
}