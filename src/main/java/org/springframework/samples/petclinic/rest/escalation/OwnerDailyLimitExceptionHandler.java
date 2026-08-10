package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 Too Many Requests with an RFC7807 {@code application/problem+json} body (type,
 * title, status, detail) when the create request would exceed the daily limit of owners created
 * today.
 */
public class OwnerDailyLimitExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner creation limit has been reached");
        detail.setProperty("errors", List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
