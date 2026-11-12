package com.databit.stockia.platform.restaurantregistration.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.restaurantregistration.application.commandservices.RestaurantRegistrationError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public final class RestaurantRegistrationHttpResponseMapper {
    private RestaurantRegistrationHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof RestaurantRegistrationError registrationError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (registrationError) {
            case RESTAURANT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EMAIL_ALREADY_TAKEN -> HttpStatus.CONFLICT;
            case INVALID_DATA -> HttpStatus.BAD_REQUEST;
            case DATABASE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, RestaurantRegistrationHttpResponseMapper::statusOf, onSuccess);
    }
}
