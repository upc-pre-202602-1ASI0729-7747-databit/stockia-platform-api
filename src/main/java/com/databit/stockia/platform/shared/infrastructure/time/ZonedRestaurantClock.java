package com.databit.stockia.platform.shared.infrastructure.time;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Clock in the restaurant time zone (stockia.restaurant.time-zone, America/Lima by default).
 */
@Component
public class ZonedRestaurantClock implements RestaurantClock {
    private final Clock clock;
    private final ZoneId zone;

    public ZonedRestaurantClock(Clock clock, @Value("${stockia.restaurant.time-zone:America/Lima}") String timeZone) {
        this.clock = clock;
        this.zone = ZoneId.of(timeZone);
    }

    @Override
    public Instant instant() {
        return clock.instant();
    }

    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.ofInstant(clock.instant(), zone);
    }

    @Override
    public LocalDate today() {
        return now().toLocalDate();
    }

    @Override
    public ZoneId zone() {
        return zone;
    }
}