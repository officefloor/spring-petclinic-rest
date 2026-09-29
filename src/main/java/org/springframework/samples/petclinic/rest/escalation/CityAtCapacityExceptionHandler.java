package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 as an RFC7807 {@code application/problem+json} body when a create-owner request
 * targets a city that has already reached its owner capacity. The {@code errors} array names the
 * offending field, mirroring the other create-conflict handlers.
 */
public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "The requested city has reached its owner capacity");
        detail.setProperty("errors", List.of("city"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
