package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to an {@link InvalidPostcodeException}: the supplied postcode was not a
 * 4-digit code or was out of range for the owner's city region. The body is an RFC7807
 * {@code application/problem+json} document.
 */
public class InvalidPostcodeHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
