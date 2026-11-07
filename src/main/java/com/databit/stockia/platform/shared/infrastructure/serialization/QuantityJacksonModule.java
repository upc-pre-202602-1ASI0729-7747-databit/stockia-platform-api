package com.databit.stockia.platform.shared.infrastructure.serialization;

import com.databit.stockia.platform.shared.domain.model.valueobjects.Quantity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Reads a quantity from a JSON number (its raw text, so no digit is lost to double precision) or
 * string, and always writes a string: JavaScript numbers cannot hold 32 decimals.
 */
@Component
public class QuantityJacksonModule extends SimpleModule {
    public QuantityJacksonModule() {
        super("QuantityModule");
        addSerializer(Quantity.class, new JsonSerializer<>() {
            @Override
            public void serialize(Quantity value, JsonGenerator generator, SerializerProvider provider) throws IOException {
                generator.writeString(value.toString());
            }
        });
        addDeserializer(Quantity.class, new JsonDeserializer<>() {
            @Override
            public Quantity deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                var token = parser.currentToken();
                if (token != JsonToken.VALUE_NUMBER_INT && token != JsonToken.VALUE_NUMBER_FLOAT && token != JsonToken.VALUE_STRING)
                    return (Quantity) context.handleUnexpectedToken(Quantity.class, parser);
                try {
                    return Quantity.parse(parser.getText());
                } catch (IllegalArgumentException exception) {
                    throw context.weirdStringException(parser.getText(), Quantity.class, "A quantity must be a number with up to 32 decimals");
                }
            }
        });
    }
}