package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict to a create-owner request whose lower-cased email is already used by
 * another owner.
 */
public class DuplicateEmailExceptionHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with this email already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
