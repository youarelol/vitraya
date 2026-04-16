package com.vitraya.adjudication.engine.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class GsonUtils {
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Convert a Java object to a JSON string.
     *
     * @param object the object to convert
     * @return the JSON string representation of the object
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        return gson.toJson(object);
    }

    /**
     * Parse a JSON string into a Java object of the specified class type.
     *
     * @param json  the JSON string
     * @param clazz the class type to parse into
     * @param <T>   the type of the desired object
     * @return the parsed Java object
     * @throws JsonSyntaxException if the JSON is not valid
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        return gson.fromJson(json, clazz);
    }

    public static <T> T fromJsonMapper(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        try {
            return new ObjectMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a JSON string into a list of Java objects of the specified class type.
     *
     * @param json  the JSON string
     * @param clazz the class type of the list elements
     * @param <T>   the type of the desired objects
     * @return the list of parsed Java objects
     * @throws JsonSyntaxException if the JSON is not valid
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        Type type = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(json, type);
    }

    public static <T> T fromJsonFile(String filePath, Class<T> clazz) {
        try (java.io.Reader reader = new java.io.FileReader(filePath)) {
            return new com.google.gson.Gson().fromJson(reader, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file: " + filePath, e);
        }
    }
}
