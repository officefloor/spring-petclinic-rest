package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body when the submitted
 * postcode is present but not a valid 4-digit value, or is out of range for the owner's
 * city region.
 */
public class OwnerPostcodeInvalidExceptionHandler {

    public void handle(@Parameter OwnerPostcodeInvalidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.BAD_REQUEST,
                "The submitted postcode is not valid for the owner's city region"));
    }
}
