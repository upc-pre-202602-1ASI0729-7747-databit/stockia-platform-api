package com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * KPIs of the business dashboard; inventoryValue and revenue in soles.
 */
public record DashboardSummaryResource(int registeredSupplies, int lowOrCriticalSupplies, int expiredSupplies,
                                       int expiringSoonSupplies, BigDecimal inventoryValue, int confirmedSales,
                                       BigDecimal revenue, BigDecimal revenueToday, int pendingAlerts,
                                       Long latestForecastId, int projectedUnitsNext7Days,
                                       BigDecimal forecastConfidence) {
}
