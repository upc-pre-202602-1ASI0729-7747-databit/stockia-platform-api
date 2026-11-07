package com.databit.stockia.platform.mlrecommendations.interfaces.rest.resources;

import java.util.List;

/**
 * One day of the forecast: projectedUnits is the total of every dish, dishName the dish with the most
 * units (what the web application chart shows), and dishes the detail per dish.
 */
public record ForecastDayResource(String date, String dayLabel, String dishName, int projectedUnits, double tempMaxC, double precipitationMm,
                                  List<DishUnitsResource> dishes) {
}
