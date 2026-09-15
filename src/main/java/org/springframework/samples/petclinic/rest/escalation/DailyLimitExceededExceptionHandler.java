package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DailyLimitExceededExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record Error(String error) {
    }

    public void handle(@Parameter DailyLimitExceededException ex, ObjectResponse<ResponseEntity<Error>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new Error(ex.getMessage())));
    }
}
