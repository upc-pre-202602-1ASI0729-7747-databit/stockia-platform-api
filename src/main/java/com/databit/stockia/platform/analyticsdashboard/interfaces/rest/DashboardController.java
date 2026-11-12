package com.databit.stockia.platform.analyticsdashboard.interfaces.rest;

import com.databit.stockia.platform.shared.application.security.CurrentUserProvider;
import com.databit.stockia.platform.analyticsdashboard.application.queries.GetDashboardSummaryQuery;
import com.databit.stockia.platform.analyticsdashboard.application.queries.GetWeeklySavingsQuery;
import com.databit.stockia.platform.analyticsdashboard.application.queryservices.DashboardQueryService;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources.DashboardSummaryResource;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.resources.WeeklySavingsResource;
import com.databit.stockia.platform.analyticsdashboard.interfaces.rest.transform.DashboardResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Business dashboard (C4: Metrics View Updater and Weekly Savings Calculator).
 */
@RestController
@RequestMapping(value = "/api/v1/dashboard", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Dashboard", description = "Business dashboard")
public class DashboardController {
    private final DashboardQueryService dashboardQueryService;
    private final CurrentUserProvider currentUser;

    public DashboardController(DashboardQueryService dashboardQueryService, CurrentUserProvider currentUser) {
        this.dashboardQueryService = dashboardQueryService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "KPIs of the restaurant")
    @GetMapping("summary")
    public DashboardSummaryResource getDashboardSummary() {
        return DashboardResourceMapper.toResourceFromValue(dashboardQueryService.handle(new GetDashboardSummaryQuery(currentUser.requireRestaurantId())));
    }

    @Operation(summary = "Weekly savings (Administrator)", description = "Estimated savings of the last 7 days: stock used before it expired "
            + "and less waste than the previous week")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("weekly-savings")
    public WeeklySavingsResource getWeeklySavings() {
        return DashboardResourceMapper.toResourceFromValue(dashboardQueryService.handle(new GetWeeklySavingsQuery(currentUser.requireRestaurantId())));
    }
}