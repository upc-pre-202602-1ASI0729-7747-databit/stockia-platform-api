package com.databit.stockia.platform.iam.application.commands;

import com.databit.stockia.platform.iam.domain.model.valueobjects.UserRole;

public record InviteMemberCommand(Long restaurantId, String fullName, String email, UserRole role, String temporaryPassword) {
}
