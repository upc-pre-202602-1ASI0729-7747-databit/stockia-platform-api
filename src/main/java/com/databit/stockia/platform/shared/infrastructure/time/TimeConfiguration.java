package com.databit.stockia.platform.shared.infrastructure.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides the system clock as a bean so the code never calls Instant.now() directly
 * (tests can replace it with a fixed clock).
 */
@Configuration
public class TimeConfiguration {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}