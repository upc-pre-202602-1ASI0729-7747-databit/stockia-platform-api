package com.databit.stockia.platform.iam.application.commands;

public record RemoveMemberCommand(Long restaurantId, Long userId, Long actingUserId) {
}
