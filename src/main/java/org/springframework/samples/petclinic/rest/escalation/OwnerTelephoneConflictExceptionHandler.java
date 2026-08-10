package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class OwnerTelephoneConflictExceptionHandler {

    public void handle(@Parameter OwnerTelephoneConflictException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with this telephone number already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
