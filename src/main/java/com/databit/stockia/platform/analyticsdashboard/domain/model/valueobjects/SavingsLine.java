package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * A supply in the savings detail: what was used before it expired, or what expired.
 */
public record SavingsLine(String itemName, String unit, BigDecimal quantity, BigDecimal value) {
}