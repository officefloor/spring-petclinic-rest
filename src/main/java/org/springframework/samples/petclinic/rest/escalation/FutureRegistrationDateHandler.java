package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Turns a {@link FutureRegistrationDateException} into a 400 naming {@code registrationDate}
 * in the body's {@code errors} array, mirroring the shape of the other owner field responses.
 */
public class FutureRegistrationDateHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request contains invalid or missing parameters");
        detail.setProperty("errors", java.util.List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
