package com.databit.stockia.platform.notificationsmessaging.application.queryservices;

import com.databit.stockia.platform.notificationsmessaging.application.queries.GetAlertsByRestaurantIdQuery;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;

import java.util.List;

public interface AlertQueryService {
    List<Alert> handle(GetAlertsByRestaurantIdQuery query);
}