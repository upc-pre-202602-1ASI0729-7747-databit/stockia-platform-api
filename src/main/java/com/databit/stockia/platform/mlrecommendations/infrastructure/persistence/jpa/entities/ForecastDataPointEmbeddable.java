package com.databit.stockia.platform.mlrecommendations.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Row of demand_forecast_data_points.
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class ForecastDataPointEmbeddable {
    @Column(name = "forecast_date", nullable = false)
    private LocalDate date;

    @Column(name = "dish_name", nullable = false, length = 120)
    private String dishName;

    @Column(name = "projected_units", nullable = false)
    private int projectedUnits;

    @Column(name = "temp_max_c", nullable = false)
    private double tempMaxC;

    @Column(name = "precipitation_mm", nullable = false)
    private double precipitationMm;
}
