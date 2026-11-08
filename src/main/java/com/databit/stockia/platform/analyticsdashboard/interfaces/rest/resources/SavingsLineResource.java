package com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources;

import java.math.BigDecimal;

public record SavingsLineResource(String itemName, String unit, BigDecimal quantity, BigDecimal value) {
}
