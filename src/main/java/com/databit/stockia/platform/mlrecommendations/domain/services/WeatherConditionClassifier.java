package com.databit.stockia.platform.mlrecommendations.domain.services;

import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DailyWeather;

/**
 * Summarizes a day as SUNNY, CLOUDY, DRIZZLE or RAIN (the web application translates the code). Uses
 * the WMO code when Open-Meteo gives one.
 */
public final class WeatherConditionClassifier {
    private WeatherConditionClassifier() {
    }

    public static String classify(DailyWeather weather) {
        var code = weather.weatherCode();
        if (weather.precipitationMm() >= 1.0 || (code != null && ((code >= 61 && code <= 67) || (code >= 80 && code <= 82)))) return "RAIN";
        if (weather.precipitationMm() >= 0.2 || (code != null && code >= 51 && code <= 57)) return "DRIZZLE";
        if (code != null && (code == 2 || code == 3 || code == 45 || code == 48)) return "CLOUDY";
        if (code != null && (code == 0 || code == 1)) return "SUNNY";
        // Climatology: Lima's sky is usually grey from June to October.
        var month = weather.date().getMonthValue();
        return month >= 6 && month <= 10 ? "CLOUDY" : "SUNNY";
    }
}
