package com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DailyWeather;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily weather forecast (Open-Meteo, with Lima climatology as fallback).
 */
public interface WeatherForecastService {
    List<DailyWeather> getDailyForecast(double latitude, double longitude, LocalDate from, int days);
}
