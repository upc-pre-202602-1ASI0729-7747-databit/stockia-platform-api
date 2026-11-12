package com.databit.stockia.platform.iam.domain;

import com.databit.stockia.platform.iam.domain.model.aggregates.User;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import com.databit.stockia.platform.iam.domain.services.AccountLockService;
import com.databit.stockia.platform.iam.domain.services.PasswordResetService;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AccountLockAndPasswordResetServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-31T15:00:00Z");

    private static User user() {
        return User.create("Carla Gallardo", " Admin@DataBiteCorp.com ", "hash", 1L, UserRole.ADMIN);
    }

    @Test
    void normalizesTheEmail() {
        assertThat(user().getEmail()).isEqualTo("admin@databitecorp.com");
    }

    @Test
    void locksTheAccountForFifteenMinutesAfterFiveWrongPasswords() {
        var user = user();
        for (var attempt = 1; attempt < AccountLockService.MAX_FAILED_ATTEMPTS; attempt++)
            assertThat(AccountLockService.registerFailedAttempt(user, NOW)).isFalse();
        assertThat(AccountLockService.remainingAttempts(user)).isEqualTo(1);

        assertThat(AccountLockService.registerFailedAttempt(user, NOW)).isTrue();
        assertThat(user.isLockedAt(NOW.plusSeconds(14 * 60))).isTrue();
        assertThat(user.isLockedAt(NOW.plusSeconds(15 * 60))).isFalse();
        assertThat(AccountLockService.minutesLeft(user, NOW)).isEqualTo(15);
        assertThat(AccountLockService.minutesLeft(user, NOW.plusSeconds(14 * 60 + 30))).isEqualTo(1);
    }

    @Test
    void unlockClearsTheCounter() {
        var user = user();
        AccountLockService.registerFailedAttempt(user, NOW);
        user.unlock();
        assertThat(user.getFailedSignInAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
    }

    @Test
    void resetCodeHasSixDigitsAndExpires() {
        var code = PasswordResetService.newCode();
        assertThat(code).matches("\\d{6}");
        var user = user().startPasswordReset("codeHash", NOW.plus(PasswordResetService.CODE_LIFETIME));
        assertThat(PasswordResetService.canUseCode(user, NOW)).isTrue();
        assertThat(PasswordResetService.canUseCode(user, NOW.plus(PasswordResetService.CODE_LIFETIME))).isFalse();
        for (var attempt = 0; attempt < PasswordResetService.MAX_ATTEMPTS; attempt++) user.registerFailedPasswordReset();
        assertThat(PasswordResetService.canUseCode(user, NOW)).isFalse();
    }

    @Test
    void masksTheEmail() {
        assertThat(PasswordResetService.maskEmail("maria@gmail.com")).isEqualTo("m***a@gmail.com");
        assertThat(PasswordResetService.maskEmail("al@x.pe")).isEqualTo("a*l@x.pe");
    }
}
