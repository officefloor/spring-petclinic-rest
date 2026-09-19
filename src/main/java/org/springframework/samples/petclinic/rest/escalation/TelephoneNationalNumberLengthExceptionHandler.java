package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class TelephoneNationalNumberLengthExceptionHandler {

    public void handle(@Parameter TelephoneNationalNumberLengthException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The telephone number's national-number length must match its country code "
                        + "('+61' requires 9 national digits, '+1' requires 10)");
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
