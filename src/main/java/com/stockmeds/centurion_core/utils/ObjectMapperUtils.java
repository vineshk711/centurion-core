package com.stockmeds.centurion_core.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapperUtils {

    private ObjectMapperUtils() {
    }

    private static final String DEFAULT_OBJECT_MAPPER = "DEFAULT_OBJECT_MAPPER";


    private static final Map<String, ObjectMapper> objectMapperMap = new HashMap<>();
    static {
        objectMapperMap.put(DEFAULT_OBJECT_MAPPER, createDefaultObjectMapper());
    }

    public static ObjectMapper createDefaultObjectMapper() {
        return initializeObjectMapper();
    }

    public static ObjectMapper getDefaultObjectMapper() {
        return objectMapperMap.get(DEFAULT_OBJECT_MAPPER);
    }


    /*
     * important for offset date times to be written out properly.
     * https://geowarin.com/correctly-handle-jsr-310-java-8-dates-with-jackson/
     * https://stackoverflow.com/questions/40488002/how-to-preserve-the-offset-while-deserializing-offsetdatetime-with-jackson/40488493
     */

    private static ObjectMapper initializeObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.getDeserializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
}