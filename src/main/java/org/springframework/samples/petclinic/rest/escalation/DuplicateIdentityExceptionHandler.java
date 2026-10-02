package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC-7807 {@link ProblemDetail} (built through the shared {@link ProblemDetails}
 * helper, like every other handler in this package) when a new owner's whole derived identity key is
 * already used by another owner.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the given identity key already exists");
        detail.setProperty("errors", java.util.List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
