package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC7807 {@code application/problem+json} body when the owner being
 * created has the same derived {@code identityKey} as an existing owner.
 */
public class OwnerIdentityDuplicateExceptionHandler {

    public void handle(@Parameter OwnerIdentityDuplicateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.CONFLICT,
                "An owner with the same identity already exists"));
    }
}
