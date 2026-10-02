package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC-7807 {@link ProblemDetail} (built through the shared {@link ProblemDetails}
 * helper, like every other handler in this package) whose {@code errors} array lists the name of each
 * required owner field that was missing or blank, e.g. {@code "errors":["city","telephone"]}.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request is missing one or more required owner fields");
        detail.setProperty("errors", ex.getFields());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
