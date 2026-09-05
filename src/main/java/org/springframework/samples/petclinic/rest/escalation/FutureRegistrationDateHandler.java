package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a {@link FutureRegistrationDateException}: the supplied registrationDate was
 * later than the server's current date. The body is an RFC7807
 * {@code application/problem+json} document.
 */
public class FutureRegistrationDateHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetails.send(response, ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
