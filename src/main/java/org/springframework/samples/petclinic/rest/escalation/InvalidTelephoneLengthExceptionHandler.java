package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class InvalidTelephoneLengthExceptionHandler {

    public void handle(@Parameter InvalidTelephoneLengthException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.respond(ex, HttpStatus.BAD_REQUEST, ex.getMessage()));
    }
}
