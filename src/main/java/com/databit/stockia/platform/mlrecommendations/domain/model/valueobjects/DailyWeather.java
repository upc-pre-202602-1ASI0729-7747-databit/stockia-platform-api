package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Weather of one day. Source: "open-meteo" or "climatology" (fallback). weatherCode is the WMO code
 * given by Open-Meteo, when available.
 */
public record DailyWeather(LocalDate date, double tempMaxC, double precipitationMm, Integer weatherCode, String source) {
}
