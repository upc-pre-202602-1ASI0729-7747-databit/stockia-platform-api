package com.databit.stockia.platform.shared.application.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Current date and time in the restaurant's time zone (America/Lima). Expiration dates,
 * "today's" sales and forecasts depend on the local calendar day, not on UTC.
 */
public interface RestaurantClock {
    Instant instant();

    ZonedDateTime now();

    LocalDate today();

    ZoneId zone();

    /**
     * @return first instant of a local day
     */
    default Instant startOf(LocalDate day) {
        return day.atStartOfDay(zone()).toInstant();
    }

    /**
     * @return first instant of the following local day (exclusive end)
     */
    default Instant endOf(LocalDate day) {
        return day.plusDays(1).atStartOfDay(zone()).toInstant();
    }

    default LocalDate localDateOf(Instant instant) {
        return instant.atZone(zone()).toLocalDate();
    }
}