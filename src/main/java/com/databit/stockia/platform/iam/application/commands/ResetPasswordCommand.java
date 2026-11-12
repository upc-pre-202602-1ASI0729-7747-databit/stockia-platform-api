package com.databit.stockia.platform.iam.application.commands;

/**
 * Sets a new password with the code of the recovery link; it also unlocks the account.
 */
public record ResetPasswordCommand(String email, String code, String newPassword) {
}
