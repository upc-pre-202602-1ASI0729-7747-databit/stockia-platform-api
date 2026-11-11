package com.databit.stockia.platform.iam.interfaces.rest.resources;

/**
 * Always the same answer, registered email or not.
 */
public record PasswordResetRequestResource(String message, String sentTo, SimulatedEmailResource simulatedEmail) {
}
