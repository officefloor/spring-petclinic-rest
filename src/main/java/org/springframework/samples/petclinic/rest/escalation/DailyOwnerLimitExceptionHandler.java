package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 with an RFC7807 problem+json body when the maximum number of owners for the day have
 * already been created (see {@link DailyOwnerLimitException}).
 */
public class DailyOwnerLimitExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        detail.setProperty("errors", List.of("dailyLimit"));
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
