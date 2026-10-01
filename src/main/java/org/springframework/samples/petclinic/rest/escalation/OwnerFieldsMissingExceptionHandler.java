package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body naming each required
 * owner field that was missing or blank.
 */
public class OwnerFieldsMissingExceptionHandler {

    public void handle(@Parameter OwnerFieldsMissingException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.BAD_REQUEST,
                "Required owner fields are missing or blank: " + String.join(", ", ex.getFields())));
    }
}
