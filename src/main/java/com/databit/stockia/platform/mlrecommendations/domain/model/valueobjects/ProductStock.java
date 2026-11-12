package com.databit.stockia.platform.mlrecommendations.domain.model.valueobjects;

import java.math.BigDecimal;

public record ProductStock(String productKey, String name, String unit, BigDecimal usableQuantity) {
}
