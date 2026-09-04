package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidPostcodeExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record InvalidPostcode(String error) {
    }

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<InvalidPostcode>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new InvalidPostcode(ex.getMessage())));
    }
}
