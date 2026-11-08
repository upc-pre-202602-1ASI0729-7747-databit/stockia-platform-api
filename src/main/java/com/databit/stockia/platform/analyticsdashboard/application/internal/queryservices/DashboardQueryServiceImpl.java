package com.databit.stockia.platform.analyticsdashboard.application.internal.queryservices;

import com.databit.stockia.platform.shared.application.time.RestaurantClock;
import com.databit.stockia.platform.analyticsdashboard.application.queries.GetDashboardSummaryQuery;
import com.databit.stockia.platform.analyticsdashboard.application.queries.GetWeeklySavingsQuery;
import com.databit.stockia.platform.analyticsdashboard.application.queryservices.DashboardQueryService;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.*;
import com.databit.stockia.platform.analyticsdashboard.domain.services.WeeklySavingsCalculator;
import com.databit.stockia.platform.mlrecommendations.interfaces.acl.MlRecommendationsContextFacade;
import com.databit.stockia.platform.mlrecommendations.interfaces.acl.MlRecommendationsContextFacade.DishProjection;
import com.databit.stockia.platform.notificationsmessaging.interfaces.acl.NotificationsMessagingContextFacade;
import com.databit.stockia.platform.recipesmanagement.interfaces.acl.RecipesManagementContextFacade;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade;
import com.databit.stockia.platform.stockmanagement.interfaces.acl.StockManagementContextFacade.InventoryLotInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardQueryServiceImpl implements DashboardQueryService {
    private final StockManagementContextFacade stockManagementContextFacade;
    private final RecipesManagementContextFacade recipesManagementContextFacade;
    private final NotificationsMessagingContextFacade notificationsMessagingContextFacade;
    private final MlRecommendationsContextFacade mlRecommendationsContextFacade;
    private final RestaurantClock clock;

    public DashboardQueryServiceImpl(StockManagementContextFacade stockManagementContextFacade,
                                     RecipesManagementContextFacade recipesManagementContextFacade,
                                     NotificationsMessagingContextFacade notificationsMessagingContextFacade,
                                     MlRecommendationsContextFacade mlRecommendationsContextFacade, RestaurantClock clock) {
        this.stockManagementContextFacade = stockManagementContextFacade;
        this.recipesManagementContextFacade = recipesManagementContextFacade;
        this.notificationsMessagingContextFacade = notificationsMessagingContextFacade;
        this.mlRecommendationsContextFacade = mlRecommendationsContextFacade;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummary handle(GetDashboardSummaryQuery query) {
        var restaurantId = query.restaurantId();
        var inventory = stockManagementContextFacade.getInventory(restaurantId);
        var sales = recipesManagementContextFacade.getSummary(restaurantId);
        var pendingAlerts = notificationsMessagingContextFacade.countPendingAlerts(restaurantId);
        var forecast = mlRecommendationsContextFacade.getLatestForecast(restaurantId);
        return new DashboardSummary(
                inventory.size(),
                (int) inventory.stream().filter(lot -> "LOW".equals(lot.status()) || "CRITICAL".equals(lot.status())).count(),
                (int) inventory.stream().filter(lot -> "EXPIRED".equals(lot.status())).count(),
                (int) inventory.stream().filter(lot -> !"EXPIRED".equals(lot.status()) && lot.daysToExpire() >= 0 && lot.daysToExpire() <= 3).count(),
                inventory.stream().map(InventoryLotInfo::stockValue).reduce(BigDecimal.ZERO, BigDecimal::add),
                sales.confirmedSales(), sales.revenue(), sales.revenueToday(), pendingAlerts,
                forecast.map(MlRecommendationsContextFacade.ForecastSummary::forecastId).orElse(null),
                forecast.map(summary -> summary.dishes().stream().mapToInt(DishProjection::totalUnits).sum()).orElse(0),
                forecast.map(MlRecommendationsContextFacade.ForecastSummary::confidenceScore).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklySavings handle(GetWeeklySavingsQuery query) {
        var restaurantId = query.restaurantId();
        var today = clock.today();
        var week = WeeklySavingsCalculator.weekEndingOn(today);
        var previousFrom = week.from().minusDays(WeeklySavingsCalculator.WEEK_DAYS);
        var consumptions = stockManagementContextFacade.getConsumptions(restaurantId, previousFrom, week.to()).stream()
                .map(entry -> new ConsumptionEntry(entry.consumedOn(), entry.itemName(), entry.unit(), entry.quantity(), entry.value(),
                        entry.daysBeforeExpiration())).toList();
        var lots = stockManagementContextFacade.getInventory(restaurantId).stream()
                .map(lot -> new LotSnapshot(lot.name(), lot.unit(), lot.quantity(), lot.expirationDate(), lot.stockValue())).toList();
        var current = recipesManagementContextFacade.getSalesBetween(restaurantId, week.from(), week.to());
        var previous = recipesManagementContextFacade.getSalesBetween(restaurantId, previousFrom, week.from().minusDays(1));
        var forecast = mlRecommendationsContextFacade.getLatestForecast(restaurantId);
        return WeeklySavingsCalculator.calculate(today, consumptions, lots,
                new SalesTotals(current.confirmedSales(), current.portionsSold(), current.revenue()),
                new SalesTotals(previous.confirmedSales(), previous.portionsSold(), previous.revenue()),
                forecast.map(summary -> summary.dishes().stream().mapToInt(DishProjection::totalUnits).sum()).orElse(0),
                forecast.map(MlRecommendationsContextFacade.ForecastSummary::confidenceScore).orElse(null));
    }
}