package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when a create-owner request omits or blanks a required field. The body is a
 * {@link ProblemDetail} whose {@code errors} array lists the name of each offending field.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request is missing or blank in one or more required fields");
        detail.setProperty("errors", ex.getFields());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
