package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

import java.util.List;

/**
 * A message key and its arguments, translated when it is shown.
 */
public record LocalizedMessage(String key, List<String> args) {
    public LocalizedMessage {
        args = args == null ? List.of() : List.copyOf(args);
    }
}
