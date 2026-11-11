package com.databit.stockia.platform.iam.domain.model.valueobjects;

/**
 * Outcome of "Forgot your password?". The response is the same whether the email exists or not;
 * simulatedEmail is only filled when the email provider is simulated and the demo mode exposes it.
 */
public record PasswordResetRequest(String maskedEmail, SimulatedEmail simulatedEmail) {
}
