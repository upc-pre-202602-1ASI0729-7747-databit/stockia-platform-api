package com.databit.stockia.platform.iam.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.iam.application.commandservices.IamError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Maps the IAM errors to HTTP status codes.
 */
public final class IamHttpResponseMapper {
    private IamHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof IamError iamError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (iamError) {
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_LOCKED -> HttpStatus.LOCKED;
            case INVALID_RESET_CODE, CURRENT_PASSWORD_INVALID, SAME_PASSWORD -> HttpStatus.UNPROCESSABLE_ENTITY;
            case EMAIL_ALREADY_TAKEN, LAST_ADMINISTRATOR, CANNOT_REMOVE_YOURSELF -> HttpStatus.CONFLICT;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DATABASE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, IamHttpResponseMapper::statusOf, onSuccess);
    }
}
