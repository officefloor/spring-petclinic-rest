package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.CityAtCapacityException;

/**
 * Responds 409 when a create request's city already holds the maximum number of owners.
 */
public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "This city has reached its maximum number of owners");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
