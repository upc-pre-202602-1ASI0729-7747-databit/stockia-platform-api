package com.databit.stockia.platform.shared.interfaces.rest;

import com.databit.stockia.platform.shared.interfaces.rest.transform.ProblemDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface layer exception handler translating validation and unexpected errors to problem
 * responses with localized messages (Accept-Language). Expected business failures do not reach
 * this class: they travel as {@code Result} values and are mapped by each bounded context.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Bean Validation failures of a request body: 400 with every invalid field in "errors".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handle(MethodArgumentNotValidException exception, Locale locale) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getFieldErrors().forEach(fieldError -> errors
                .computeIfAbsent(fieldError.getField(), field -> new java.util.ArrayList<>())
                .add(messageSource.getMessage(fieldError, locale)));
        var detail = errors.entrySet().stream().map(entry -> entry.getKey() + ": " + String.join(" ", entry.getValue()))
                .collect(Collectors.joining(" | "));
        log.debug("Validation failed: {}", detail);
        var problem = ProblemDetails.of(HttpStatus.BAD_REQUEST, message("validation.title", locale), detail, null);
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Domain invariants and value objects reject invalid data with a message key.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handle(IllegalArgumentException exception, Locale locale) {
        var key = exception.getMessage() != null ? exception.getMessage() : "validation.title";
        return ProblemDetails.response(HttpStatus.BAD_REQUEST, "InvalidData", message(key, locale));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> handleUnreadable(Exception exception, Locale locale) {
        log.debug("Unreadable request: {}", exception.getMessage());
        return ProblemDetails.response(HttpStatus.BAD_REQUEST, "MalformedRequest", message("request.malformed", locale));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handle(AccessDeniedException exception, Locale locale) {
        return ProblemDetails.response(HttpStatus.FORBIDDEN, "Forbidden", message("security.forbidden", locale));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handle(NoResourceFoundException exception, Locale locale) {
        return ProblemDetails.response(HttpStatus.NOT_FOUND, "NotFound", message("request.notFound", locale));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handle(HttpRequestMethodNotSupportedException exception, Locale locale) {
        return ProblemDetails.response(HttpStatus.METHOD_NOT_ALLOWED, "MethodNotAllowed", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, Locale locale) {
        log.error("Unexpected error", exception);
        return ProblemDetails.response(HttpStatus.INTERNAL_SERVER_ERROR, message("server.unexpected.title", locale),
                message("server.unexpected.detail", locale));
    }

    private String message(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }
}