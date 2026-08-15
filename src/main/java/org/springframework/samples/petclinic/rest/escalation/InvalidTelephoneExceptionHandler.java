package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class InvalidTelephoneExceptionHandler {

    /** Body shape: {@code {"errors": ["telephone"]}}. */
    public record InvalidField(List<String> errors) {
    }

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<InvalidField>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new InvalidField(List.of("telephone"))));
    }
}
