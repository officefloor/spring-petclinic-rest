package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.escalation.ProblemDetails;

/**
 * Responds 429 Too Many Requests with an RFC7807 {@code application/problem+json} body when 100 or
 * more owners already carry the current day's registration date.
 */
public class OwnerDailyLimitReachedExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitReachedException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.responseEntity(ex, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
    }
}
