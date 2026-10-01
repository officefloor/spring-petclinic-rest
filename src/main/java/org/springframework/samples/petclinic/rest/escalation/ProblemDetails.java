package org.springframework.samples.petclinic.rest.escalation;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Builds {@link ProblemDetail} bodies with the fields common to every escalation handler
 * (title = exception simple name, timestamp, empty schemaValidationErrors placeholder).
 */
public final class ProblemDetails {

    private ProblemDetails() {
    }

    public static ProblemDetail build(Exception ex, HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setTitle(ex.getClass().getSimpleName());
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        return problemDetail;
    }

    /**
     * Builds the problem body and wraps it in a {@link ResponseEntity} carrying the same
     * {@code status}, so the status is named once at the call site rather than on both the body
     * and the entity. For handlers that need to attach extra members, call {@link #build} and wrap
     * with {@link #asResponse} instead.
     */
    public static ResponseEntity<ProblemDetail> responseEntity(Exception ex, HttpStatus status,
            String detail) {
        return asResponse(build(ex, status, detail));
    }

    /**
     * Wraps an already-built {@link ProblemDetail} in a {@link ResponseEntity} whose HTTP status is
     * taken from the problem body, keeping the status a single source of truth.
     */
    public static ResponseEntity<ProblemDetail> asResponse(ProblemDetail detail) {
        return ResponseEntity.status(detail.getStatus()).body(detail);
    }
}
