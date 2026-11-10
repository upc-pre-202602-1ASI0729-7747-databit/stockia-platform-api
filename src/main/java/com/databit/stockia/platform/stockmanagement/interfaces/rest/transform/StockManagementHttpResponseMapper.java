package com.databit.stockia.platform.stockmanagement.interfaces.rest.transform;

import com.databit.stockia.platform.shared.application.result.ApplicationError;
import com.databit.stockia.platform.shared.application.result.CommandFailure;
import com.databit.stockia.platform.shared.application.result.Result;
import com.databit.stockia.platform.shared.interfaces.rest.transform.CommandResultHttpResponseMapper;
import com.databit.stockia.platform.stockmanagement.application.commandservices.StockManagementError;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Maps the Stock Management errors to HTTP status codes.
 */
public final class StockManagementHttpResponseMapper {
    private StockManagementHttpResponseMapper() {
    }

    public static HttpStatus statusOf(ApplicationError error) {
        if (!(error instanceof StockManagementError stockError)) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (stockError) {
            case INVENTORY_ITEM_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_DATA -> HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_STOCK -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T, CommandFailure> result, MessageSource messageSource,
                                                         Function<T, ResponseEntity<?>> onSuccess) {
        return CommandResultHttpResponseMapper.toResponseEntity(result, messageSource, StockManagementHttpResponseMapper::statusOf, onSuccess);
    }
}
