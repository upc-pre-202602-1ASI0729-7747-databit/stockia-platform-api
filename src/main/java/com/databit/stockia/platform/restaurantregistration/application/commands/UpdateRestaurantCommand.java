package com.databit.stockia.platform.restaurantregistration.application.commands;

public record UpdateRestaurantCommand(Long restaurantId, String name, String address, String phone) {
}
