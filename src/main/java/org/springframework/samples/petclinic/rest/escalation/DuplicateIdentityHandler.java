package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a {@link DuplicateIdentityException}: the create-owner identity key
 * (normalized telephone, email and household id) is already used by another owner. The
 * body is an RFC7807 {@code application/problem+json} document.
 */
public class DuplicateIdentityHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT, ex.getMessage());
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
