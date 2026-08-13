package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 with an RFC7807 {@code application/problem+json} body when 100 or more
 * owners have already been created today.
 */
public class DailyLimitExceptionHandler {

    public void handle(@Parameter DailyLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily limit for creating owners has been reached");
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
