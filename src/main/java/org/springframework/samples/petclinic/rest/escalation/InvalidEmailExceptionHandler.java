package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidEmailExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record InvalidEmail(String error) {
    }

    public void handle(@Parameter InvalidEmailException ex,
            ObjectResponse<ResponseEntity<InvalidEmail>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new InvalidEmail(ex.getMessage())));
    }
}
