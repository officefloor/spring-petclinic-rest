package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class DailyOwnerLimitExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.TOO_MANY_REQUESTS,
                "The daily limit for creating new owners has been reached");
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(detail));
    }
}
