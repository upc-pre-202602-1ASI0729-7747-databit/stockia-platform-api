package com.databit.stockia.platform.notificationsmessaging.application.internal.queryservices;

import com.databit.stockia.platform.notificationsmessaging.application.queries.GetAlertsByRestaurantIdQuery;
import com.databit.stockia.platform.notificationsmessaging.application.queryservices.AlertQueryService;
import com.databit.stockia.platform.notificationsmessaging.domain.model.aggregates.Alert;
import com.databit.stockia.platform.notificationsmessaging.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertQueryServiceImpl implements AlertQueryService {
    private final AlertRepository alertRepository;

    public AlertQueryServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> handle(GetAlertsByRestaurantIdQuery query) {
        return alertRepository.findAllByRestaurantId(query.restaurantId());
    }
}