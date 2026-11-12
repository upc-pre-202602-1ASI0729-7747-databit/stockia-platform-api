package com.databit.stockia.platform.iam.application.internal.outboundservices.tokens;

import com.databit.stockia.platform.shared.application.security.AuthenticatedUser;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;

import java.util.Optional;

public interface TokenService {
    String generateToken(User user);

    /**
     * @return the user the token was issued to, or empty when it is invalid or expired
     */
    Optional<AuthenticatedUser> validateToken(String token);
}
