package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidPostcodeException;

/**
 * Responds 400 with an RFC 7807 {@code application/problem+json} body when an owner create
 * request's supplied {@code postcode} is not four digits or is out of range for the city's
 * region. The {@code errors} array names {@code postcode}.
 */
public class InvalidPostcodeExceptionHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied postcode is not valid for the city's region", List.of("postcode"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
