package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 to a {@link DailyLimitExceededException}: 100 or more owners have already
 * been registered today, so no further owners may be created until tomorrow. The body is
 * an RFC7807 {@code application/problem+json} document.
 */
public class DailyLimitExceededHandler {

    public void handle(@Parameter DailyLimitExceededException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetails.send(response, ex, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }
}
