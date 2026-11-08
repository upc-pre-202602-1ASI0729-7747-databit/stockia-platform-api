package com.databit.stockia.platform.iam.domain.model.valueobjects;

import java.time.Instant;

/**
 * Copy of the email the simulated provider "sent" (demo only).
 */
public record SimulatedEmail(String to, String subject, String resetLink, String code, Instant expiresAt) {
}
