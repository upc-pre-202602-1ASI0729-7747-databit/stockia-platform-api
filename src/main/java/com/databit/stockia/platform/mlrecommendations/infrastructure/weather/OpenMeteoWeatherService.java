package com.databit.stockia.platform.mlrecommendations.infrastructure.weather;

import com.databit.stockia.platform.mlrecommendations.application.internal.outboundservices.WeatherForecastService;
import com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects.DailyWeather;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

/**
 * Open-Meteo forecast API (https://open-meteo.com): free, no API key — the third-party service of the
 * platform. Any network error or missing day falls back to Lima's climatology, so a forecast is always
 * produced.
 */
@Slf4j
@Service
public class OpenMeteoWeatherService implements WeatherForecastService {
    private final boolean enabled;
    private final String forecastUrl;
    private final RestClient restClient;

    public OpenMeteoWeatherService(@Value("${stockia.weather.enabled:true}") boolean enabled,
                                   @Value("${stockia.weather.forecast-url:https://api.open-meteo.com/v1/forecast}") String forecastUrl) {
        this.enabled = enabled;
        this.forecastUrl = forecastUrl;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(8000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public List<DailyWeather> getDailyForecast(double latitude, double longitude, LocalDate from, int days) {
        var result = new ArrayList<>(IntStream.range(0, days).mapToObj(offset -> LimaClimatology.forDate(from.plusDays(offset))).toList());
        if (!enabled) return result;
        var url = String.format(Locale.ROOT,
                "%s?latitude=%s&longitude=%s&daily=temperature_2m_max,precipitation_sum,weather_code&timezone=America%%2FLima&start_date=%s&end_date=%s",
                forecastUrl, latitude, longitude, from, from.plusDays(days - 1L));
        try {
            var body = restClient.get().uri(URI.create(url)).retrieve().body(JsonNode.class);
            if (body == null) return result;
            var daily = body.required("daily");
            var dates = daily.required("time");
            var temps = daily.required("temperature_2m_max");
            var rains = daily.required("precipitation_sum");
            var codes = daily.required("weather_code");
            for (var index = 0; index < dates.size(); index++) {
                var date = LocalDate.parse(dates.get(index).asText());
                var position = (int) java.time.temporal.ChronoUnit.DAYS.between(from, date);
                if (position < 0 || position >= result.size() || !temps.get(index).isNumber()) continue;
                result.set(position, new DailyWeather(date, temps.get(index).asDouble(), rains.get(index).isNumber() ? rains.get(index).asDouble() : 0,
                        codes.get(index).isNumber() ? codes.get(index).asInt() : null, "open-meteo"));
            }
        } catch (RuntimeException exception) {
            log.warn("Open-Meteo not available ({}); using Lima climatology", exception.getMessage());
        }
        return result;
    }
}
