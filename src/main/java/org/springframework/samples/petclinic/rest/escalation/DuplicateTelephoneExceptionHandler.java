package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class DuplicateTelephoneExceptionHandler {

    public void handle(@Parameter DuplicateTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with this telephone already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
