package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with an RFC-7807 {@link ProblemDetail} (built through the shared {@link ProblemDetails}
 * helper, like every other handler in this package) when a new owner resolves to the same household
 * (same last name and postcode) as an existing owner and did not set {@code sharesHousehold}.
 */
public class HouseholdDuplicateExceptionHandler {

    public void handle(@Parameter HouseholdDuplicateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT,
                "An owner in the same household already exists");
        detail.setProperty("errors", java.util.List.of("sharesHousehold"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
