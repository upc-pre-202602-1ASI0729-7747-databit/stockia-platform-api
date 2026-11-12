package com.databit.stockia.platform.iam.application.commands;

/**
 * "Forgot your password?": sends a recovery link to the email if it is registered.
 */
public record RequestPasswordResetCommand(String email) {
}
