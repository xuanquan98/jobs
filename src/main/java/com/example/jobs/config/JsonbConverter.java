package com.example.jobs.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonbConverter implements AttributeConverter<JsonNode, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode jsonNode) {
        try {
            return jsonNode != null ? jsonNode.toString() : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert JSON to string", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String jsonString) {
        try {
            return jsonString != null ? objectMapper.readTree(jsonString) : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert string to JSON", e);
        }
    }
}