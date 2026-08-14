package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 when a create-owner request derives an {@code identityKey} whose whole value is
 * already used by another owner. The body is a {@link ProblemDetail}.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "The identity key is already used by another owner");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
