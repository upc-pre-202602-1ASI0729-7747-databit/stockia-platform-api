package com.databit.stockia.platform.analyticsdashboard.application.queryservices;

import com.databit.stockia.platform.analyticsdashboard.application.queries.GetDashboardSummaryQuery;
import com.databit.stockia.platform.analyticsdashboard.application.queries.GetWeeklySavingsQuery;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.DashboardSummary;
import com.databit.stockia.platform.analyticsdashboard.domain.model.valueobjects.WeeklySavings;

public interface DashboardQueryService {
    DashboardSummary handle(GetDashboardSummaryQuery query);

    WeeklySavings handle(GetWeeklySavingsQuery query);
}
