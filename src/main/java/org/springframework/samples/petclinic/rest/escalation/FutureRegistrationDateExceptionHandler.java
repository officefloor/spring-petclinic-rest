package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FutureRegistrationDateExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record FutureRegistrationDate(String error) {
    }

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<FutureRegistrationDate>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new FutureRegistrationDate(ex.getMessage())));
    }
}
