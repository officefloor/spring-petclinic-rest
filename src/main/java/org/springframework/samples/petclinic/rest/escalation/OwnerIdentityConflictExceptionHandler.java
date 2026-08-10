package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC7807 {@code application/problem+json} body (type, title, status,
 * detail) when a create-owner request's whole identityKey matches an existing owner's.
 */
public class OwnerIdentityConflictExceptionHandler {

    public void handle(@Parameter OwnerIdentityConflictException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same identity already exists");
        detail.setProperty("errors", List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
