package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body (type, title, status,
 * detail) when the supplied postcode is malformed or out of range for the owner's city.
 */
public class OwnerPostcodeInvalidExceptionHandler {

    public void handle(@Parameter OwnerPostcodeInvalidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied postcode is not valid for the owner's city");
        detail.setProperty("errors", List.of("postcode"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
