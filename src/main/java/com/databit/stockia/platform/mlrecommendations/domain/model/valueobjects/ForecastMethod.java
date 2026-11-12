package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

/**
 * MACHINE_LEARNING: ONNX model trained in ml/. BASELINE: average of the same weekday of the last 4
 * weeks, used when the model is not loaded or the restaurant has less than 14 days of sales.
 */
public enum ForecastMethod {
    MACHINE_LEARNING,
    BASELINE
}
