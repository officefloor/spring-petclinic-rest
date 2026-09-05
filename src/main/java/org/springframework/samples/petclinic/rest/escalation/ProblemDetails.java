package org.springframework.samples.petclinic.rest.escalation;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;

/**
 * Builds {@link ProblemDetail} bodies with the fields common to every escalation handler
 * (title = exception simple name, timestamp, empty schemaValidationErrors placeholder) and
 * sends them as the {@code status}-coded {@link ResponseEntity} every handler responds with.
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

    /** Sends {@code detail} as a {@code status}-coded {@code application/problem+json} response. */
    static void send(ObjectResponse<ResponseEntity<ProblemDetail>> response, HttpStatus status,
            ProblemDetail detail) {
        response.send(ResponseEntity.status(status).body(detail));
    }

    /** Builds a {@link ProblemDetail} for {@code ex} and sends it as a {@code status} response. */
    static void send(ObjectResponse<ResponseEntity<ProblemDetail>> response, Exception ex,
            HttpStatus status, String detail) {
        send(response, status, build(ex, status, detail));
    }
}
