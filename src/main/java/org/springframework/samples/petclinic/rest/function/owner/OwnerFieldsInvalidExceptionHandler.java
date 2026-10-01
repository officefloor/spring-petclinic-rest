package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.escalation.ProblemDetails;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body. The offending field names are
 * carried in the {@code errors} extension member alongside the standard problem members.
 */
public class OwnerFieldsInvalidExceptionHandler {

    public void handle(@Parameter OwnerFieldsInvalidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request is missing or blank in one or more required owner fields");
        detail.setProperty("errors", ex.getErrors());
        response.send(ProblemDetails.asResponse(detail));
    }
}
