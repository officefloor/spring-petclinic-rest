package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body (type, title, status,
 * detail) whose {@code errors} array lists the name of each required owner field that was
 * missing or blank.
 */
public class OwnerFieldsRequiredExceptionHandler {

    public void handle(@Parameter OwnerFieldsRequiredException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "Missing or blank required owner fields: " + ex.getErrors());
        detail.setProperty("errors", ex.getErrors());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
