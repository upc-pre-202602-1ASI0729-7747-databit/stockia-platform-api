package com.databit.stockia.platform.notificationsmessaging.domain.model.valueobjects;

import java.math.BigDecimal;

public record StockSnapshot(Long inventoryItemId, String name, String unit, BigDecimal quantity,
                            BigDecimal minThreshold, long daysToExpire, String status) {
}
