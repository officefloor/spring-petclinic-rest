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
 * Builds the RFC 7807 {@link ProblemDetail} bodies shared by every escalation handler
 * (title = exception simple name, timestamp, empty schemaValidationErrors placeholder) and
 * wraps them in a {@link ResponseEntity} carrying the {@code application/problem+json}
 * content type, so a handler never has to restate either concern.
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

    /** Build the problem body and wrap it as an {@code application/problem+json} response. */
    static ResponseEntity<ProblemDetail> respond(Exception ex, HttpStatus status, String detail) {
        return asResponse(build(ex, status, detail));
    }

    /**
     * Wrap an already-built (and possibly enriched) problem body as an
     * {@code application/problem+json} response using the body's own status.
     */
    static ResponseEntity<ProblemDetail> asResponse(ProblemDetail detail) {
        return ResponseEntity.status(detail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }
}
