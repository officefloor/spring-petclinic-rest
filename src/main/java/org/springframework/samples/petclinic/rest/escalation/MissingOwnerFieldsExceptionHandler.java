package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<Errors>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Errors(ex.getFields())));
    }

    /** Response body: {@code {"errors": ["city", ...]}}. */
    public record Errors(List<String> errors) {
    }
}
