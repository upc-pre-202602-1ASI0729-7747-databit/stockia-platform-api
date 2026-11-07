package com.databit.stockia.platform.mlrecommendations.application.commands;

public record ApplyRecommendationCommand(Long recommendationId, Long restaurantId) {
}
