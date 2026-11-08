package com.databit.stockia.platform.iam.application.commandservices;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;

/**
 * A signed-in user and its bearer token.
 */
public record AuthenticatedSession(User user, String token) {
}
