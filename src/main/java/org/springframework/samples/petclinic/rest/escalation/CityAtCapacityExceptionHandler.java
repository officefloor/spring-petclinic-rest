package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a create-owner request whose city already contains the maximum number of
 * owners, with an RFC7807 application/problem+json body whose 'errors' array names 'city'.
 */
public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "The city already contains the maximum number of owners");
        detail.setProperty("errors", List.of("city"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
