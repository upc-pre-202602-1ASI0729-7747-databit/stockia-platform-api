package com.databit.stockia.platform.shared.application.result;

import java.util.Objects;

/**
 * Expected failure of a command: an error of the bounded context catalog, the key and arguments of
 * its localized message and optional details for the client (e.g. the supplies that block a sale).
 *
 * @param error       the error (its code is the problem title)
 * @param messageKey  key of the localized detail (by default the error's own key)
 * @param messageArgs arguments of the localized message
 * @param details     extra data returned as "details" in the problem response, or null
 */
public record CommandFailure(ApplicationError error, String messageKey, Object[] messageArgs, Object details) {
    public CommandFailure {
        Objects.requireNonNull(error);
        messageKey = messageKey == null ? error.messageKey() : messageKey;
        messageArgs = messageArgs == null ? new Object[0] : messageArgs;
    }

    public static CommandFailure of(ApplicationError error, Object... messageArgs) {
        return new CommandFailure(error, null, messageArgs, null);
    }

    /**
     * @return a failure with the same code but another message, e.g. RecipeNotFound with "The recipe to sell was not found."
     */
    public static CommandFailure withMessage(ApplicationError error, String messageKey, Object... messageArgs) {
        return new CommandFailure(error, messageKey, messageArgs, null);
    }

    public CommandFailure withDetails(Object details) {
        return new CommandFailure(error, messageKey, messageArgs, details);
    }
}