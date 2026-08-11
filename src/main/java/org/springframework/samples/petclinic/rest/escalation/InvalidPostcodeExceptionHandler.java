package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidPostcodeExceptionHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<Errors>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Errors(List.of("postcode"))));
    }

    /** Response body: {@code {"errors": ["postcode"]}}. */
    public record Errors(List<String> errors) {
    }
}
