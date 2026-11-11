package com.databit.stockia.platform.restaurantregistration.interfaces.rest.resources;

public record RestaurantResource(Long id, String name, String address, String phone, double latitude, double longitude) {
}
