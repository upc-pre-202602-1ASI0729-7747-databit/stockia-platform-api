package com.databit.stockia.platform.shared.domain.model.valueobjects;

import java.util.Locale;
import java.util.Optional;

/**
 * Converts the UPPER_SNAKE codes used by the Web Application (e.g. "LOW_STOCK") to enum constants.
 */
public final class EnumCodes {
    private EnumCodes() {
    }

    public static <E extends Enum<E>> Optional<E> parse(Class<E> type, String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        var normalized = code.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (var constant : type.getEnumConstants()) {
            if (constant.name().equals(normalized) || constant.name().replace("_", "").equals(normalized.replace("_", "")))
                return Optional.of(constant);
        }
        return Optional.empty();
    }
}