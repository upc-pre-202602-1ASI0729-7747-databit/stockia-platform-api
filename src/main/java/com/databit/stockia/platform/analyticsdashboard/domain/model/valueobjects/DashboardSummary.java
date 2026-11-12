package com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * KPIs of the business dashboard; inventory value and revenue in soles.
 */
public record DashboardSummary(int registeredSupplies, int lowOrCriticalSupplies, int expiredSupplies,
                               int expiringSoonSupplies, BigDecimal inventoryValue, int confirmedSales,
                               BigDecimal revenue, BigDecimal revenueToday, int pendingAlerts,
                               Long latestForecastId, int projectedUnitsNext7Days, BigDecimal forecastConfidence) {
}