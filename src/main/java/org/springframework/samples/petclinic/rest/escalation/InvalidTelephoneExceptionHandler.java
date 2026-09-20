package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidTelephoneException;

/**
 * Responds 400 when a create request's telephone cannot be converted to valid E.164 form.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The telephone number must form a valid E.164 number (8 to 15 digits) with the "
                        + "correct national-number length for its country code "
                        + "('+61' requires 9 national digits, '+1' requires 10)");
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
