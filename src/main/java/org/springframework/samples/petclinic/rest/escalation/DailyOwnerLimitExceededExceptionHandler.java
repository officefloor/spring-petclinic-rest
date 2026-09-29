package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 (Too Many Requests) as an RFC7807 {@code application/problem+json} body when a
 * create-owner request would exceed the daily owner creation limit. The {@code errors} array names
 * the offending field, mirroring the other create-conflict handlers.
 */
public class DailyOwnerLimitExceededExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitExceededException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner creation limit has been exceeded");
        detail.setProperty("errors", List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
