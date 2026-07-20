package org.springframework.samples.petclinic.rest.escalation;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Builds {@link ProblemDetail} bodies with the fields common to every escalation handler
 * (title = exception simple name, timestamp, empty schemaValidationErrors placeholder).
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
}
