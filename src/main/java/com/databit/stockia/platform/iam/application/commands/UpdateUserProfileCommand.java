package com.databit.stockia.platform.iam.application.commands;

public record UpdateUserProfileCommand(Long userId, String fullName, String email) {
}
