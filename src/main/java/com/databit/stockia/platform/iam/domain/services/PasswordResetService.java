package com.databit.stockia.platform.iam.domain.services;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * Password Reset Service (C4): a 6-digit code valid for CODE_LIFETIME, at most MAX_ATTEMPTS tries. The
 * code travels in the recovery link sent by email; only its hash is stored.
 */
public final class PasswordResetService {
    public static final Duration CODE_LIFETIME = Duration.ofMinutes(30);
    public static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordResetService() {
    }

    public static String newCode() {
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    /**
     * @return whether the user has a reset in progress that can still be used
     */
    public static boolean canUseCode(User user, Instant now) {
        return user.getPasswordResetCodeHash() != null && user.getPasswordResetExpiresAt() != null
                && user.getPasswordResetExpiresAt().isAfter(now) && user.getPasswordResetAttempts() < MAX_ATTEMPTS;
    }

    /**
     * "maria@gmail.com" becomes "m***a@gmail.com" (to confirm where the link went without exposing the address).
     */
    public static String maskEmail(String email) {
        var at = email.indexOf('@');
        if (at <= 1) return email;
        var name = email.substring(0, at);
        return name.charAt(0) + "*".repeat(Math.max(1, name.length() - 2)) + name.charAt(name.length() - 1) + email.substring(at);
    }
}
