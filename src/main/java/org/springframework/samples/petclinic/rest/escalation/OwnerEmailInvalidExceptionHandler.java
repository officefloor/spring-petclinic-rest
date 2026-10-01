package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body when the submitted
 * email is present but not a syntactically valid address.
 */
public class OwnerEmailInvalidExceptionHandler {

    public void handle(@Parameter OwnerEmailInvalidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.BAD_REQUEST,
                "The submitted email is not a syntactically valid address"));
    }
}
