package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stock a sale used from a lot (input of the Weekly Savings Calculator).
 */
public record ConsumptionEntry(LocalDate consumedOn, String itemName, String unit,
                               BigDecimal quantity, BigDecimal value, long daysBeforeExpiration) {
}
