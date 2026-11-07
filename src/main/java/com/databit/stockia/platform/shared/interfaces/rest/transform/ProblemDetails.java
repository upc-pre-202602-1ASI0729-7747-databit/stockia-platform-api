package com.databit.stockia.platform.shared.interfaces.rest.transform;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;

/**
 * Builds RFC 9457 problem responses the same way in every bounded context. The title is the error
 * code (e.g. "EmailAlreadyTaken") the Web Application maps to its own i18n messages; the detail is
 * a localized text; "details" carries extra data when the client needs it.
 */
public final class ProblemDetails {
    private ProblemDetails() {
    }

    public static ProblemDetail of(HttpStatus status, String title, String detail, Object details) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://httpstatuses.com/" + status.value()));
        if (details != null) problem.setProperty("details", details);
        return problem;
    }

    public static ResponseEntity<ProblemDetail> response(HttpStatus status, String title, String detail) {
        return ResponseEntity.status(status).body(of(status, title, detail, null));
    }
}