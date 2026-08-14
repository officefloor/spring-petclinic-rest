package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when a create-owner request supplies a {@code registrationDate} that is later than
 * the server's current date. The body is a {@link ProblemDetail}.
 */
public class FutureRegistrationDateExceptionHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied registration date must not be later than the server date");
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
