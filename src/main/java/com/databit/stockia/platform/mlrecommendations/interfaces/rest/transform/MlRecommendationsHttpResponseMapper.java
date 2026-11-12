package com.databit.stockia.platform.mlrecommendations.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.mlrecommendations.application.commandservices.MlRecommendationsError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public final class MlRecommendationsHttpResponseMapper {
    private MlRecommendationsHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof MlRecommendationsError mlError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (mlError) {
            case NO_DISHES -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INVALID_HORIZON -> HttpStatus.BAD_REQUEST;
            case RECOMMENDATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, MlRecommendationsHttpResponseMapper::statusOf, onSuccess);
    }
}
