package com.databit.stockia.platform.mlrecommendations.domain.services;

import java.time.LocalDate;

/**
 * Peruvian calendar used as model features. Same rules as ml/stockia_ml/data/calendar_pe.py (checked
 * against the "holidays" package for 2020-2030 in the Python tests).
 */
public final class PeruCalendar {
    /**
     * {month, day, first year it applies}.
     */
    private static final int[][] FIXED_HOLIDAYS = {
            {1, 1, 1900},   // New Year
            {5, 1, 1900},   // Labour Day
            {6, 7, 2024},   // Battle of Arica and Flag Day (Law 31788)
            {6, 29, 1900},  // Saint Peter and Saint Paul
            {7, 23, 2023},  // Peruvian Air Force Day (Law 31822)
            {7, 28, 1900},  // Independence Day
            {7, 29, 1900},  // Independence Day
            {8, 6, 2022},   // Battle of Junín
            {8, 30, 1900},  // Saint Rose of Lima
            {10, 8, 1900},  // Battle of Angamos
            {11, 1, 1900},  // All Saints
            {12, 8, 1900},  // Immaculate Conception
            {12, 9, 2022},  // Battle of Ayacucho
            {12, 25, 1900}  // Christmas
    };

    private PeruCalendar() {
    }

    /**
     * Anonymous Gregorian algorithm (Meeus/Jones/Butcher).
     */
    public static LocalDate easterSunday(int year) {
        var a = year % 19;
        var b = year / 100;
        var c = year % 100;
        var d = b / 4;
        var e = b % 4;
        var f = (b + 8) / 25;
        var g = (b - f + 1) / 3;
        var h = (19 * a + b - d - g + 15) % 30;
        var i = c / 4;
        var k = c % 4;
        var l = (32 + 2 * e + 2 * i - h - k) % 7;
        var m = (a + 11 * h + 22 * l) / 451;
        var month = (h + l - 7 * m + 114) / 31;
        var day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }

    public static boolean isHoliday(LocalDate day) {
        for (var holiday : FIXED_HOLIDAYS)
            if (day.getYear() >= holiday[2] && day.getMonthValue() == holiday[0] && day.getDayOfMonth() == holiday[1]) return true;
        var easter = easterSunday(day.getYear());
        return day.equals(easter.minusDays(3)) || day.equals(easter.minusDays(2)); // Holy Thursday and Good Friday
    }

    public static boolean isHolidayEve(LocalDate day) {
        return isHoliday(day.plusDays(1));
    }

    /**
     * Quincena: payday on the 15th and the last day of the month, and the day after.
     */
    public static boolean isPaydayWindow(LocalDate day) {
        var dayOfMonth = day.getDayOfMonth();
        return dayOfMonth == 1 || dayOfMonth == 15 || dayOfMonth == 16 || dayOfMonth == day.lengthOfMonth();
    }

    /**
     * @return Monday = 0 … Sunday = 6 (Python's date.weekday())
     */
    public static int mondayBasedDayOfWeek(LocalDate day) {
        return day.getDayOfWeek().getValue() - 1;
    }
}
