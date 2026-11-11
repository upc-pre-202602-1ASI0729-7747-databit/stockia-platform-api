package com.databit.stockia.platform.iam.infrastructure.tokens.jwt.services;

import com.databit.stockia.platform.shared.application.security.AuthenticatedUser;
import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Issues and validates JWTs (HS256). Claims: sub (user id), email, restaurant_id and role (ADMIN /
 * EMPLOYEE) — the contract with the shared CurrentUserProvider.
 */
@Slf4j
@Service
public class TokenServiceImpl implements BearerTokenService {
    private static final String AUTHORIZATION_PARAMETER_NAME = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private final SecretKey key;
    private final Duration expiration;

    public TokenServiceImpl(@Value("${authorization.jwt.secret}") String secret,
                            @Value("${authorization.jwt.expiration.days:7}") int expirationDays) {
        if (secret == null || secret.length() < 32)
            throw new IllegalStateException("authorization.jwt.secret (STOCKIA_JWT_SECRET) must have at least 32 characters.");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofDays(expirationDays);
    }

    @Override
    public String generateToken(User user) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("restaurant_id", user.getRestaurantId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<AuthenticatedUser> validateToken(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            var restaurantId = claims.get("restaurant_id", Number.class);
            return Optional.of(new AuthenticatedUser(Long.valueOf(claims.getSubject()), restaurantId == null ? null : restaurantId.longValue(),
                    claims.get("email", String.class), claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("Invalid JSON Web Token: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        var parameter = request.getHeader(AUTHORIZATION_PARAMETER_NAME);
        if (parameter == null || !parameter.startsWith(BEARER_TOKEN_PREFIX)) return null;
        return parameter.substring(BEARER_TOKEN_PREFIX.length()).trim();
    }
}
