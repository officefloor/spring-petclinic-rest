package org.springframework.samples.petclinic.rest.escalation;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Builds RFC7807 {@link ProblemDetail} bodies with the fields common to every escalation
 * handler (type, title = exception simple name, status, detail, timestamp and an empty
 * schemaValidationErrors placeholder) and wraps them in a {@link ResponseEntity} whose
 * {@code Content-Type} is {@code application/problem+json}.
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
        problemDetail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        return problemDetail;
    }

    /**
     * Wrap an already-built {@link ProblemDetail} as an {@code application/problem+json}
     * response, taking the HTTP status from the detail itself.
     */
    static ResponseEntity<ProblemDetail> asResponse(ProblemDetail detail) {
        return ResponseEntity.status(detail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }

    /** Build a problem detail and wrap it as an {@code application/problem+json} response. */
    static ResponseEntity<ProblemDetail> asResponse(Exception ex, HttpStatus status, String detail) {
        return asResponse(build(ex, status, detail));
    }
}
