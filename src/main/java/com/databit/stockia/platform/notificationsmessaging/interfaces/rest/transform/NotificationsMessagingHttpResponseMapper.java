package com.databit.stockia.platform.notificationsmessaging.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.notificationsmessaging.application.commandservices.NotificationsMessagingError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public final class NotificationsMessagingHttpResponseMapper {
    private NotificationsMessagingHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof NotificationsMessagingError alertError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (alertError) {
            case ALERT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_DATA -> HttpStatus.BAD_REQUEST;
            case NOT_DELIVERED, ALREADY_DELIVERED -> HttpStatus.CONFLICT;
            case DELIVERY_FAILED -> HttpStatus.BAD_GATEWAY;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, NotificationsMessagingHttpResponseMapper::statusOf, onSuccess);
    }
}