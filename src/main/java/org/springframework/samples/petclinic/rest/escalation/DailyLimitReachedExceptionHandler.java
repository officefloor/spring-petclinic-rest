package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 when 100 or more owners have already been registered today. The body is a
 * {@link ProblemDetail}.
 */
public class DailyLimitReachedExceptionHandler {

    public void handle(@Parameter DailyLimitReachedException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The maximum number of owners for today has already been reached");
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
