package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidTelephoneExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record InvalidTelephone(String error) {
    }

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<InvalidTelephone>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new InvalidTelephone(ex.getMessage())));
    }
}
