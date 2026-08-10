package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body {@code {"error": ...}} when a create-owner request supplies a
 * {@code registrationDate} later than the server date.
 */
public class OwnerRegistrationDateFutureExceptionHandler {

    public void handle(@Parameter OwnerRegistrationDateFutureException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage())));
    }
}
