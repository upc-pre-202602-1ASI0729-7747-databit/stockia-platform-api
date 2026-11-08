package com.databit.stockia.platform.shared.application.result;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Error catalog of a bounded context, implemented by an enum (e.g. IamError.INVALID_CREDENTIALS).
 * The code ("InvalidCredentials") is the title of the problem response the Web Application maps
 * to its own i18n messages; the message key resolves the localized detail.
 */
public interface ApplicationError {
    /**
     * @return the enum constant name (provided by {@link Enum})
     */
    String name();

    /**
     * @return prefix of the message keys of the bounded context, e.g. "iam.error"
     */
    String messageKeyPrefix();

    /**
     * @return the error code in PascalCase, e.g. INVALID_CREDENTIALS → "InvalidCredentials"
     */
    default String code() {
        return Arrays.stream(name().toLowerCase(Locale.ROOT).split("_"))
                .map(part -> part.isEmpty() ? part : Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining());
    }

    default String messageKey() {
        return messageKeyPrefix() + "." + code();
    }
}