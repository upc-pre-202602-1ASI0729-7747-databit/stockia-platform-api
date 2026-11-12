package com.databit.stockia.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point of the StockIA Platform (RESTful API of StockIA, by DataBit).
 * Scheduling runs the hourly inventory alert sweep.
 *
 * @since 1.0.0
 */
@EnableScheduling
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class StockiaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockiaPlatformApplication.class, args);
    }
}
