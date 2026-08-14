package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body whose {@code errors} array names each required owner field that
 * was missing or blank on a create-owner request.
 */
public class OwnerFieldsValidationExceptionHandler {

    /** Response body: {@code {"errors": ["city", ...]}}. */
    public record Errors(List<String> errors) {
    }

    public void handle(@Parameter OwnerFieldsValidationException ex,
            ObjectResponse<ResponseEntity<Errors>> response) {
        response.send(ResponseEntity.badRequest().body(new Errors(ex.getErrors())));
    }
}
