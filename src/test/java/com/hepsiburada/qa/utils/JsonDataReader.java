package com.hepsiburada.qa.utils;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDataReader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> List<T> readList(String resourcePath, TypeReference<List<T>> typeReference) {
        try (InputStream is = JsonDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource file not found on classpath: " + resourcePath);
            }
            return objectMapper.readValue(is, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data from JSON resource: " + resourcePath, e);
        }
    }

    public static <T> T readObject(String resourcePath, Class<T> clazz) {
        try (InputStream is = JsonDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource file not found on classpath: " + resourcePath);
            }
            return objectMapper.readValue(is, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data from JSON resource: " + resourcePath, e);
        }
    }
}
