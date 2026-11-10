package com.databit.stockia.platform.recipesmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.recipesmanagement.application.commandservices.RecipesManagementError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Maps the Recipes Management errors to HTTP status codes. CannotSell is 422 with details {dish, shortages[]}
 * so the web application shows which supply is missing or expired.
 */
public final class RecipesManagementHttpResponseMapper {
    private RecipesManagementHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof RecipesManagementError recipesError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (recipesError) {
            case RECIPE_NOT_FOUND, SALE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INGREDIENT_NOT_FOUND, CANNOT_SELL -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INVALID_DATA -> HttpStatus.BAD_REQUEST;
            case ALREADY_VOIDED -> HttpStatus.CONFLICT;
            case DATABASE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, RecipesManagementHttpResponseMapper::statusOf, onSuccess);
    }
}
