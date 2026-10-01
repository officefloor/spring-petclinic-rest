package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.escalation.ProblemDetails;

/**
 * Responds 409 Conflict with an RFC7807 {@code application/problem+json} body when a create
 * request's city already contains 50 or more owners.
 */
public class OwnerCityAtCapacityExceptionHandler {

    public void handle(@Parameter OwnerCityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.responseEntity(ex, HttpStatus.CONFLICT, ex.getMessage()));
    }
}
