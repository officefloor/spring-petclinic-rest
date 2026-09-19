package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class HouseholdDuplicateExceptionHandler {

    public void handle(@Parameter HouseholdDuplicateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner already exists in this household");
        detail.setProperty("householdId", ex.getHouseholdId());
        detail.setProperty("existingOwnerId", ex.getExistingOwnerId());
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
