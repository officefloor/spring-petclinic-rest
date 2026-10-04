package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateHouseholdException;

/**
 * Responds 409 when an owner create request shares both a last name and an address with an
 * existing owner and did not opt in via {@code sharesHousehold}.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with the same last name and address already exists");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
