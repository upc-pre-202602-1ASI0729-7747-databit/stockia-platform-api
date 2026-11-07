package com.databit.stockia.platform.mlrecommendations.infrastructure.weather;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DailyWeather;

import java.time.LocalDate;

/**
 * Monthly climatology of Lima (mean daily max °C, mean daily mm). Same table as
 * ml/stockia_ml/config.py; used when Open-Meteo is not reachable.
 */
public final class LimaClimatology {
    private static final double[][] MONTHS = {
            {26.5, 0.05}, {27.5, 0.10}, {27.0, 0.05}, {25.0, 0.02}, {22.5, 0.05}, {20.0, 0.15},
            {19.0, 0.25}, {18.5, 0.30}, {19.0, 0.20}, {20.5, 0.10}, {22.5, 0.05}, {24.5, 0.05}};

    private LimaClimatology() {
    }

    public static DailyWeather forDate(LocalDate date) {
        var month = MONTHS[date.getMonthValue() - 1];
        return new DailyWeather(date, month[0], month[1], null, "climatology");
    }
}
