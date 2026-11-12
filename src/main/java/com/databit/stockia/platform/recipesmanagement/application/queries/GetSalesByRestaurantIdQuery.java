package com.databit.stockia.platform.recipesmanagement.application.queries;

import java.time.LocalDate;

/**
 * Sales of a restaurant, optionally between two local dates (inclusive).
 */
public record GetSalesByRestaurantIdQuery(Long restaurantId, LocalDate from, LocalDate to) {
}
