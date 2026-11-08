package com.databit.stockia.platform.iam.application.commands;

/**
 * An Administrator unlocks a member locked by wrong passwords.
 */
public record UnlockUserCommand(Long restaurantId, Long userId) {
}
