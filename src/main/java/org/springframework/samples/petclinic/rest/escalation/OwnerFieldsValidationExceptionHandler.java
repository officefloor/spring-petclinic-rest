package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body when a create-owner request is
 * missing or blank in a required field. The offending field names are carried in the {@code errors}
 * extension member alongside the standard {@code type}, {@code title}, {@code status} and
 * {@code detail} members.
 */
public class OwnerFieldsValidationExceptionHandler {

    public void handle(@Parameter OwnerFieldsValidationException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request is missing or blank in one or more required fields");
        detail.setProperty("errors", ex.getErrors());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
