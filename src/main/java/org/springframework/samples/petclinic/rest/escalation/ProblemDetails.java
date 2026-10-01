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
 * Builds the RFC7807 {@code application/problem+json} responses shared by every escalation
 * handler. {@link #build} stamps the fields common to each body (title = exception simple
 * name, timestamp, empty schemaValidationErrors placeholder); {@link #response} wraps a body
 * in the finished {@link ResponseEntity} so the status and content type are decided here in
 * one place rather than repeated per handler.
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

    /** Build the body and wrap it as an {@code application/problem+json} response. */
    static ResponseEntity<ProblemDetail> response(Exception ex, HttpStatus status, String detail) {
        return response(build(ex, status, detail));
    }

    /**
     * Wrap an already-built (possibly enriched) {@link ProblemDetail} as an
     * {@code application/problem+json} response, taking the status from the body.
     */
    static ResponseEntity<ProblemDetail> response(ProblemDetail detail) {
        return ResponseEntity.status(detail.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }
}
