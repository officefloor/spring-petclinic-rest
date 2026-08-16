package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request whose supplied registrationDate is later than the server
 * date, with an RFC7807 application/problem+json body whose 'errors' array names 'registrationDate'.
 */
public class FutureRegistrationDateExceptionHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The registration date must not be later than the current server date");
        detail.setProperty("errors", List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
