package com.databit.stockia.platform.iam.infrastructure.persistence.jpa.entities;

import com.databit.stockia.platform.shared.infrastructure.persistence.jpa.entities.AuditableJpaEntity;
import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Table users. Only BCrypt hashes are stored.
 */
@Getter
@Setter
@Entity
@Table(name = "User", uniqueConstraints = @UniqueConstraint(name = "ux_users_email", columnNames = "email"),
        indexes = @Index(name = "ix_users_restaurant_id", columnList = "restaurant_id"))
public class UserJpaEntity extends AuditableJpaEntity {
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(name = "failed_sign_in_attempts", nullable = false)
    private int failedSignInAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "password_reset_code_hash", length = 100)
    private String passwordResetCodeHash;

    @Column(name = "password_reset_expires_at")
    private Instant passwordResetExpiresAt;

    @Column(name = "password_reset_attempts", nullable = false)
    private int passwordResetAttempts;
}
