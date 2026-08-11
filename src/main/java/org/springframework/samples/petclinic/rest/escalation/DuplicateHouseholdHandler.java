package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Turns a {@link DuplicateHouseholdException} into a 409 naming {@code lastName} and
 * {@code address} in the body's {@code errors} array, mirroring the shape of the other
 * owner-field responses.
 */
public class DuplicateHouseholdHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same last name and address already exists");
        detail.setProperty("errors", java.util.List.of("lastName", "address"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
