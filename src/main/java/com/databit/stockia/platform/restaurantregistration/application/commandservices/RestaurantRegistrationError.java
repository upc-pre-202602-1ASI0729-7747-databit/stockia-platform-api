package com.databit.stockia.platform.restaurantregistration.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

/**
 * Errors of the Restaurant Registration bounded context (their codes are the problem titles).
 */
public enum RestaurantRegistrationError implements ApplicationError {
    RESTAURANT_NOT_FOUND,
    EMAIL_ALREADY_TAKEN,
    INVALID_DATA,
    DATABASE_ERROR;

    @Override
    public String messageKeyPrefix() {
        return "restaurant.error";
    }
}
