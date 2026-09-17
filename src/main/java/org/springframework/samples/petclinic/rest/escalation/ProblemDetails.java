package org.springframework.samples.petclinic.rest.escalation;

import java.net.URI;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Builds RFC7807 {@link ProblemDetail} bodies with the members common to every escalation
 * handler: {@code type} (about:blank), {@code title} (exception simple name), {@code status},
 * {@code detail}, plus a {@code timestamp}. Handlers add any handler-specific members themselves.
 */
final class ProblemDetails {

    private ProblemDetails() {
    }

    static ProblemDetail build(Exception ex, HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
