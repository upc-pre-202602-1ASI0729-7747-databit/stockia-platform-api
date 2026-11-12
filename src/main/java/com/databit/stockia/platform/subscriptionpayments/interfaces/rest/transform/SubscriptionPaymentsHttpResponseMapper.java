package com.databit.stockia.platform.subscriptionpayments.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.subscriptionpayments.application.commandservices.SubscriptionPaymentsError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public final class SubscriptionPaymentsHttpResponseMapper {
    private SubscriptionPaymentsHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof SubscriptionPaymentsError subscriptionError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (subscriptionError) {
            case PLAN_NOT_FOUND, SUBSCRIPTION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_PAYMENT_METHOD -> HttpStatus.BAD_REQUEST;
            case PAYMENT_REJECTED -> HttpStatus.PAYMENT_REQUIRED;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, SubscriptionPaymentsHttpResponseMapper::statusOf, onSuccess);
    }
}
