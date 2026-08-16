package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request with missing or blank required fields, with an
 * RFC7807 application/problem+json body whose 'errors' array lists the name of each such field.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "One or more required owner fields are missing or blank");
        detail.setProperty("errors", ex.getFields());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
