package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC7807 {@code application/problem+json} body (type, title, status,
 * detail) when the create request targets a city that already contains 50 or more owners.
 */
public class OwnerCityCapacityExceptionHandler {

    public void handle(@Parameter OwnerCityCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "The city has reached its owner capacity");
        detail.setProperty("errors", List.of("city"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
