package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a create request shares another owner's household (same
 * lastName and address) without opting in via {@code sharesHousehold}.
 */
public class DuplicateOwnerHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateOwnerHouseholdException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner with this last name already lives at this address");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
