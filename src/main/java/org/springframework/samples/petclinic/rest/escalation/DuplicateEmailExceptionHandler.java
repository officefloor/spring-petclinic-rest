package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DuplicateEmailExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record Error(String error) {
    }

    public void handle(@Parameter DuplicateEmailException ex, ObjectResponse<ResponseEntity<Error>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(new Error(ex.getMessage())));
    }
}
