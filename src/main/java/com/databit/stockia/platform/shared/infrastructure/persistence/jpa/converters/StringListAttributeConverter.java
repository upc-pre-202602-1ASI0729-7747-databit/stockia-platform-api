package com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a short list of strings (plan features, delivered channels) as a JSON array in one column.
 */
@Converter
public class StringListAttributeConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> values) {
        try {
            return MAPPER.writeValueAsString(values == null ? List.of() : values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        try {
            return new ArrayList<>(MAPPER.readValue(text, new TypeReference<List<String>>() {
            }));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }
}