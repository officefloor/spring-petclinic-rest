package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class DuplicateOwnerEmailExceptionHandler {

    public void handle(@Parameter DuplicateOwnerEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.respond(ProblemDetails.build(ex, HttpStatus.CONFLICT, ex.getMessage())));
    }
}
