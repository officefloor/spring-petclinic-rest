package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 Bad Request with a JSON body whose {@code errors} array lists the name
 * of each invalid or missing field, e.g. {@code {"errors":["city","telephone"]}}.
 */
public class InvalidOwnerFieldsExceptionHandler {

    /** Response body carrying the list of offending field names. */
    public record ValidationErrors(List<String> errors) {
    }

    public void handle(@Parameter InvalidOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<ValidationErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new ValidationErrors(ex.getFields())));
    }
}
