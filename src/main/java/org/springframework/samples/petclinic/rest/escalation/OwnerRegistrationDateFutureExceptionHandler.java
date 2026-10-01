package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body whose {@code errors} array names {@code registrationDate} when the
 * supplied registration date is later than the server's current date.
 */
public class OwnerRegistrationDateFutureExceptionHandler {

    public void handle(@Parameter OwnerRegistrationDateFutureException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("registrationDate"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }
}
