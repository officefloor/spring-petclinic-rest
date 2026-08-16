package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 (Too Many Requests) to a create-owner request made once the day's owner
 * registration limit has been reached, with an RFC7807 application/problem+json body whose
 * 'errors' array names 'registrationDate'.
 */
public class DailyOwnerLimitExceededExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitExceededException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily owner registration limit has been reached");
        detail.setProperty("errors", List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
