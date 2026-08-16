package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create/update-owner request whose postcode is malformed or out of range for
 * the owner's city region, with an RFC7807 application/problem+json body whose 'errors' array
 * names 'postcode'.
 */
public class InvalidPostcodeExceptionHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The postcode is malformed or out of range for the city's region");
        detail.setProperty("errors", List.of("postcode"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
