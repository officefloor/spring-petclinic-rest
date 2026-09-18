package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a create request targets a city that has already reached its
 * owner capacity.
 */
public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "This city has reached its maximum number of owners");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
