package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 with an RFC7807 {@code application/problem+json} body when 100 or more owners
 * have already been created today.
 */
public class OwnerDailyLimitExceededExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitExceededException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        detail.setProperty("errors", List.of("dailyLimit"));
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON).body(detail));
    }
}
