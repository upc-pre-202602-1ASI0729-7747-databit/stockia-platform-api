package com.databit.stockia.platform.mlrecommendations.domain;

import com.databit.stockia.platform.mlrecommendations.domain.services.PeruCalendar;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeruCalendarTest {
    @Test
    void computesEasterSunday() {
        assertThat(PeruCalendar.easterSunday(2024)).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(PeruCalendar.easterSunday(2026)).isEqualTo(LocalDate.of(2026, 4, 5));
    }

    @Test
    void knowsFixedAndMovableHolidays() {
        assertThat(PeruCalendar.isHoliday(LocalDate.of(2026, 7, 28))).isTrue();
        assertThat(PeruCalendar.isHoliday(LocalDate.of(2026, 4, 2))).isTrue();
        assertThat(PeruCalendar.isHoliday(LocalDate.of(2026, 4, 3))).isTrue();
        assertThat(PeruCalendar.isHoliday(LocalDate.of(2023, 6, 7))).isFalse();
        assertThat(PeruCalendar.isHoliday(LocalDate.of(2024, 6, 7))).isTrue();
        assertThat(PeruCalendar.isHolidayEve(LocalDate.of(2026, 12, 24))).isTrue();
    }

    @Test
    void knowsThePaydayWindowAndTheWeekday() {
        assertThat(PeruCalendar.isPaydayWindow(LocalDate.of(2026, 2, 28))).isTrue();
        assertThat(PeruCalendar.isPaydayWindow(LocalDate.of(2026, 3, 1))).isTrue();
        assertThat(PeruCalendar.isPaydayWindow(LocalDate.of(2026, 3, 2))).isFalse();
        assertThat(PeruCalendar.mondayBasedDayOfWeek(LocalDate.of(2026, 10, 26))).isZero();
        assertThat(PeruCalendar.mondayBasedDayOfWeek(LocalDate.of(2026, 11, 1))).isEqualTo(6);
    }
}
