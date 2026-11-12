package com.databit.stockia.platform.shared.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Translates a command {@link Result} into a {@link ResponseEntity}. Each bounded context passes
 * the function that maps its errors to HTTP status codes.
 */
public final class CommandResultHttpResponseMapper {
    private CommandResultHttpResponseMapper() {
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<ApplicationError, HttpStatus> statusOf,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return result.fold(onSuccess, failure -> toResponseEntity(failure, messageSource, statusOf));
    }

    public static ResponseEntity<?> toResponseEntity(CommandFailure failure, MessageSource messageSource,
                                                     Function<ApplicationError, HttpStatus> statusOf) {
        var status = statusOf.apply(failure.error());
        var detail = messageSource.getMessage(failure.messageKey(), failure.messageArgs(), failure.error().code(),
                LocaleContextHolder.getLocale());
        return ResponseEntity.status(status).body(ProblemDetails.of(status, failure.error().code(), detail, failure.details()));
    }
}