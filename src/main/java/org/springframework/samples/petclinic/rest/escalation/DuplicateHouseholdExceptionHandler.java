package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Turns a {@link DuplicateHouseholdException} into a 409 when a create-owner request
 * duplicates an existing owner's last name and address without opting in to sharing a
 * household.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "Another owner with the same last name already lives at this address");
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
