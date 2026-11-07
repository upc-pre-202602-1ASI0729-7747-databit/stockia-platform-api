package com.databit.stockia.platform.mlrecommendations.domain.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Builds the model features of one dish for one day. Same definitions and order as
 * ml/stockia_ml/features.py (FEATURE_ORDER); the API checks the order against model_metadata.json when
 * it loads the model, and the tests compare the result with parity_fixture.json.
 */
public final class DemandFeatureBuilder {
    public static final List<String> FEATURE_ORDER = List.of(
            "lag_7", "lag_14", "mean_7_13", "mean_7_34", "same_weekday_mean_4w", "day_of_week", "is_weekend", "day_of_month",
            "month", "is_holiday", "is_holiday_eve", "is_payday_window", "temp_max_c", "precipitation_mm");
    /**
     * Oldest day a feature reads: t-34.
     */
    public static final int HISTORY_DAYS_REQUIRED = 34;
    /**
     * Newest day a feature reads: t-7, so the next 7 days are forecast directly.
     */
    public static final int MINIMUM_LAG_DAYS = 7;

    private DemandFeatureBuilder() {
    }

    /**
     * @param history         units sold per day; missing days count as 0
     * @param target          day to forecast
     * @param tempMaxC        maximum temperature of that day (°C)
     * @param precipitationMm precipitation of that day (mm)
     */
    public static float[] build(Map<LocalDate, Double> history, LocalDate target, double tempMaxC, double precipitationMm) {
        var dayOfWeek = PeruCalendar.mondayBasedDayOfWeek(target);
        return new float[]{
                (float) units(history, target, 7),
                (float) units(history, target, 14),
                (float) mean(history, target, IntStream.range(7, 14).toArray()),
                (float) mean(history, target, IntStream.range(7, 35).toArray()),
                (float) mean(history, target, new int[]{7, 14, 21, 28}),
                dayOfWeek,
                dayOfWeek >= 5 ? 1f : 0f,
                target.getDayOfMonth(),
                target.getMonthValue(),
                PeruCalendar.isHoliday(target) ? 1f : 0f,
                PeruCalendar.isHolidayEve(target) ? 1f : 0f,
                PeruCalendar.isPaydayWindow(target) ? 1f : 0f,
                (float) tempMaxC,
                (float) precipitationMm
        };
    }

    /**
     * Baseline: same weekday of the last 4 weeks, or the 4-week mean when that is zero.
     */
    public static double baseline(float[] features) {
        var sameWeekday = features[4];
        return sameWeekday > 0 ? sameWeekday : features[3];
    }

    private static double units(Map<LocalDate, Double> history, LocalDate target, int daysBack) {
        return history.getOrDefault(target.minusDays(daysBack), 0d);
    }

    private static double mean(Map<LocalDate, Double> history, LocalDate target, int[] daysBack) {
        var total = 0d;
        for (var back : daysBack) total += units(history, target, back);
        return total / daysBack.length;
    }
}
