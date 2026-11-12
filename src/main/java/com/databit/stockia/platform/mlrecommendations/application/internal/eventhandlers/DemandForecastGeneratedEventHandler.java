package com.databit.stockia.platform.mlrecommendations.application.internal.eventhandlers;

import com.databit.stockia.platform.mlrecommendations.application.commands.GenerateRecommendationsCommand;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.RecommendationCommandService;
import com.databit.stockia.platform.mlrecommendations.domain.model.events.DemandForecastGeneratedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Every new forecast refreshes the purchase and menu recommendations (after the forecast is committed).
 */
@Slf4j
@Service
public class DemandForecastGeneratedEventHandler {
    private final RecommendationCommandService recommendationCommandService;

    public DemandForecastGeneratedEventHandler(RecommendationCommandService recommendationCommandService) {
        this.recommendationCommandService = recommendationCommandService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(DemandForecastGeneratedEvent event) {
        try {
            recommendationCommandService.handle(new GenerateRecommendationsCommand(event.restaurantId(), event.demandForecastId()));
        } catch (RuntimeException exception) {
            log.warn("Recommendations could not be generated for forecast {}", event.demandForecastId(), exception);
        }
    }
}
