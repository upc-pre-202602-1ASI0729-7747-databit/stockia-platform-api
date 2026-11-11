package com.databit.stockia.platform.iam.interfaces.rest.resources;

import java.time.Instant;

/**
 * Copy of the simulated email (only in demo mode).
 */
public record SimulatedEmailResource(String to, String subject, String resetLink, String code, Instant expiresAt) {
}
