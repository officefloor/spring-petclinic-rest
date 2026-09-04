package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC7807 problem+json body when a create-owner request's derived
 * {@code identityKey} exactly equals an existing owner's identityKey.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT, ex.getMessage());
        detail.setProperty("errors", List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
