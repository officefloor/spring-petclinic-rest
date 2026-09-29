package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 as an RFC7807 {@code application/problem+json} body when a create-owner request's
 * whole {@code identityKey} is already used by another owner. The {@code errors} array names the
 * offending field, mirroring the other create-conflict handlers.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same identity already exists");
        detail.setProperty("errors", List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
