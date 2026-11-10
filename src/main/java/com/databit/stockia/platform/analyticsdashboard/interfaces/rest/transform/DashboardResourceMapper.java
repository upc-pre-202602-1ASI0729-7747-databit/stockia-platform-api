package com.databit.stockia.platform.analyticsdashboard.interfaces.rest.transform;

import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.DashboardSummary;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.SavingsLine;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.WeeklySavings;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources.DashboardSummaryResource;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources.SavingsLineResource;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources.WeeklySavingsResource;

public final class DashboardResourceMapper {
    private DashboardResourceMapper() {
    }

    public static DashboardSummaryResource toResourceFromValue(DashboardSummary summary) {
        return new DashboardSummaryResource(summary.registeredSupplies(), summary.lowOrCriticalSupplies(), summary.expiredSupplies(),
                summary.expiringSoonSupplies(), summary.inventoryValue(), summary.confirmedSales(), summary.revenue(), summary.revenueToday(),
                summary.pendingAlerts(), summary.latestForecastId(), summary.projectedUnitsNext7Days(), summary.forecastConfidence());
    }

    public static WeeklySavingsResource toResourceFromValue(WeeklySavings savings) {
        return new WeeklySavingsResource(savings.from().toString(), savings.to().toString(), savings.estimatedSavings(), savings.rescuedValue(),
                savings.previousRescuedValue(), savings.wasteValue(), savings.previousWasteValue(), savings.wasteReduction(),
                savings.confirmedSales(), savings.portionsSold(), savings.revenue(), savings.previousRevenue(), savings.projectedUnitsNextWeek(),
                savings.forecastConfidence(), savings.topRescued().stream().map(DashboardResourceMapper::toLine).toList(),
                savings.wasted().stream().map(DashboardResourceMapper::toLine).toList());
    }

    private static SavingsLineResource toLine(SavingsLine line) {
        return new SavingsLineResource(line.itemName(), line.unit(), line.quantity(), line.value());
    }
}