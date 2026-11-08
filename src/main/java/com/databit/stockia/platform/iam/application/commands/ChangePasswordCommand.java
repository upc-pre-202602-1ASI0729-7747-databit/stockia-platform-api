package com.databit.stockia.platform.iam.application.commands;

public record ChangePasswordCommand(Long userId, String currentPassword, String newPassword) {
}
