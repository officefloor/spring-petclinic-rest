package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateIdentityException;

/**
 * Responds 409 when a create request's identity key (telephone, email and last-name Soundex)
 * matches an existing owner's — a re-registration of the same identity.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same identity already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
