package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 (Too Many Requests) with an RFC7807 {@code application/problem+json} body
 * when the maximum number of owners have already been created today.
 */
public class OwnerDailyLimitExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached"));
    }
}
