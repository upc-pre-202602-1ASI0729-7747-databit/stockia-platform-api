package com.databit.stockia.platform.iam.infrastructure.tokens.jwt;

import com.databit.stockia.platform.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Token service that also reads the bearer token of a request.
 */
public interface BearerTokenService extends TokenService {
    String getBearerTokenFrom(HttpServletRequest request);
}
