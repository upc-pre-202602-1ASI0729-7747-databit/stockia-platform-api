package com.databit.stockia.platform.iam.domain.model.aggregates;

import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * A member of the restaurant team. Only BCrypt hashes are stored (password and password-reset code)
 * and they are never serialized.
 */
public class User {
    private Long id;
    private String fullName;
    private String email;
    private String passwordHash;
    private final Long restaurantId;
    private UserRole role;
    private int failedSignInAttempts;
    private Instant lockedUntil;
    private String passwordResetCodeHash;
    private Instant passwordResetExpiresAt;
    private int passwordResetAttempts;

    private User(Long id, String fullName, String email, String passwordHash, Long restaurantId, UserRole role) {
        this.id = id;
        this.fullName = Objects.requireNonNull(fullName).trim();
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.restaurantId = Objects.requireNonNull(restaurantId);
        this.role = Objects.requireNonNull(role);
    }

    public static User create(String fullName, String email, String passwordHash, Long restaurantId, UserRole role) {
        return new User(null, fullName, email, passwordHash, restaurantId, role);
    }

    public static User rehydrate(Long id, String fullName, String email, String passwordHash, Long restaurantId, UserRole role,
                                 int failedSignInAttempts, Instant lockedUntil, String passwordResetCodeHash,
                                 Instant passwordResetExpiresAt, int passwordResetAttempts) {
        var user = new User(id, fullName, email, passwordHash, restaurantId, role);
        user.failedSignInAttempts = failedSignInAttempts;
        user.lockedUntil = lockedUntil;
        user.passwordResetCodeHash = passwordResetCodeHash;
        user.passwordResetExpiresAt = passwordResetExpiresAt;
        user.passwordResetAttempts = passwordResetAttempts;
        return user;
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public User updateProfile(String fullName, String email) {
        this.fullName = fullName.trim();
        this.email = normalizeEmail(email);
        return this;
    }

    public User updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public User changeRole(UserRole role) {
        this.role = role;
        return this;
    }

    /**
     * Counts a wrong password; reaching the limit locks the account until lockUntil.
     */
    public User registerFailedSignIn(int maxAttempts, Instant lockUntil) {
        failedSignInAttempts++;
        if (failedSignInAttempts >= maxAttempts) lockedUntil = lockUntil;
        return this;
    }

    /**
     * A successful sign-in, an Administrator or a password reset clears the lock.
     */
    public User unlock() {
        failedSignInAttempts = 0;
        lockedUntil = null;
        return this;
    }

    public User startPasswordReset(String codeHash, Instant expiresAt) {
        passwordResetCodeHash = codeHash;
        passwordResetExpiresAt = expiresAt;
        passwordResetAttempts = 0;
        return this;
    }

    public User registerFailedPasswordReset() {
        passwordResetAttempts++;
        return this;
    }

    public User clearPasswordReset() {
        passwordResetCodeHash = null;
        passwordResetExpiresAt = null;
        passwordResetAttempts = 0;
        return this;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Long getRestaurantId() { return restaurantId; }
    public UserRole getRole() { return role; }
    public int getFailedSignInAttempts() { return failedSignInAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }
    public String getPasswordResetCodeHash() { return passwordResetCodeHash; }
    public Instant getPasswordResetExpiresAt() { return passwordResetExpiresAt; }
    public int getPasswordResetAttempts() { return passwordResetAttempts; }
}
