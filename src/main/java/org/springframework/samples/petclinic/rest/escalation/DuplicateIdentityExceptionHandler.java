package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a create-owner request whose derived identityKey already matches an existing
 * owner, with an RFC7807 application/problem+json body whose 'errors' array names 'identityKey'.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same derived identity already exists");
        detail.setProperty("errors", List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
