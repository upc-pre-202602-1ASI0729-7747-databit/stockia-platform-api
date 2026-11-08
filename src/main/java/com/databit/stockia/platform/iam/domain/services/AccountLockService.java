package com.databit.stockia.platform.iam.domain.services;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;

import java.time.Duration;
import java.time.Instant;

/**
 * Account Lock Service (C4): after MAX_FAILED_ATTEMPTS wrong passwords in a row the account is locked
 * for LOCK_DURATION. A successful sign-in resets the count.
 */
public final class AccountLockService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private AccountLockService() {
    }

    /**
     * Registers a wrong password.
     *
     * @return whether the account is locked now
     */
    public static boolean registerFailedAttempt(User user, Instant now) {
        user.registerFailedSignIn(MAX_FAILED_ATTEMPTS, now.plus(LOCK_DURATION));
        return user.isLockedAt(now);
    }

    public static int remainingAttempts(User user) {
        return Math.max(0, MAX_FAILED_ATTEMPTS - user.getFailedSignInAttempts());
    }

    /**
     * @return whole minutes left (at least 1) until the account opens again
     */
    public static int minutesLeft(User user, Instant now) {
        if (user.getLockedUntil() == null) return 0;
        var seconds = Duration.between(now, user.getLockedUntil()).toSeconds();
        return (int) Math.max(1, (seconds + 59) / 60);
    }
}
