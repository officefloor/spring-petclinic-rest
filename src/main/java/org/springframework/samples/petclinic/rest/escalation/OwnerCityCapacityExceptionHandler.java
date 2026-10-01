package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC7807 {@code application/problem+json} body when the owner being
 * created belongs to a city that already contains the maximum number of owners.
 */
public class OwnerCityCapacityExceptionHandler {

    public void handle(@Parameter OwnerCityCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.CONFLICT,
                "The owner's city has reached its maximum number of owners"));
    }
}
