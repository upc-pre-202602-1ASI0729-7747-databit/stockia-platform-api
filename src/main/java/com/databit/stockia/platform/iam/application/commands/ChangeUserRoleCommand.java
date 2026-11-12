package com.databit.stockia.platform.iam.application.commands;

import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;

public record ChangeUserRoleCommand(Long restaurantId, Long userId, UserRole role) {
}
