package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateTelephoneException;

/**
 * Responds 409 when a create request's normalized telephone is already used by another
 * owner.
 */
public class DuplicateTelephoneExceptionHandler {

    public void handle(@Parameter DuplicateTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with this telephone number already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
