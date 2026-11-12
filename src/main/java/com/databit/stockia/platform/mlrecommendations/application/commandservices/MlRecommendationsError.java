package com.databit.stockia.platform.mlrecommendations.application.commandservices;

import com.databit.stockia.platform.shared.application.result.ApplicationError;

public enum MlRecommendationsError implements ApplicationError {
    NO_DISHES,
    INVALID_HORIZON,
    RECOMMENDATION_NOT_FOUND;

    @Override
    public String messageKeyPrefix() {
        return "ml.error";
    }
}
