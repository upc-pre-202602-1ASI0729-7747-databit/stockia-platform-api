package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A supply lot as it is now (input of the Weekly Savings Calculator).
 */
public record LotSnapshot(String name, String unit, BigDecimal quantity, LocalDate expirationDate,
                          BigDecimal stockValue) {
}