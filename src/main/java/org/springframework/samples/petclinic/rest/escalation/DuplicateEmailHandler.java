package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Turns a {@link DuplicateEmailException} into a 409 naming {@code email} in the body's
 * {@code errors} array, mirroring the shape of the other owner-field responses.
 */
public class DuplicateEmailHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "Email is already used by another owner");
        detail.setProperty("errors", java.util.List.of("email"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
