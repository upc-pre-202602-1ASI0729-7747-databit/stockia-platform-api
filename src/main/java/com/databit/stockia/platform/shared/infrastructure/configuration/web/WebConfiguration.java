package com.databit.stockia.platform.shared.infrastructure.configuration.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Web settings shared by every bounded context:
 * <ul>
 *     <li>Language from the Accept-Language header: English (default) or Latin American Spanish.</li>
 *     <li>CORS for the Angular Web Application (stockia.cors.allowed-origins accepts patterns such as https://*.vercel.app).</li>
 * </ul>
 */
@Configuration
public class WebConfiguration {
    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("es")));
        return resolver;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${stockia.cors.allowed-origins:http://localhost:4200}") String origins) {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.stream(origins.split(",")).map(String::trim).filter(origin -> !origin.isEmpty()).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Language", "Location"));
        configuration.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}