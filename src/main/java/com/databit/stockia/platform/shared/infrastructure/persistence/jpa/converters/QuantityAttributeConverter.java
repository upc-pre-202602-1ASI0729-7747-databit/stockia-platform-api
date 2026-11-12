package com.databit.stockia.platform.shared.infrastructure.persistence.jpa.converters;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Stores a {@link Quantity} as text: MySQL DECIMAL keeps 30 decimals at most and quantities have 32.
 */
@Converter
public class QuantityAttributeConverter implements AttributeConverter<Quantity, String> {
    @Override
    public String convertToDatabaseColumn(Quantity quantity) {
        return quantity == null ? null : quantity.toString();
    }

    @Override
    public Quantity convertToEntityAttribute(String text) {
        return text == null ? null : Quantity.parse(text);
    }
}