package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when a create-owner request supplies a telephone that cannot form a valid E.164 number
 * (8 to 15 digits after the '+'). The body is a {@link ProblemDetail}.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The telephone must form a valid E.164 number (8 to 15 digits after the '+')");
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
