package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.FutureRegistrationDateException;

/**
 * Responds 400 when a create-owner request supplies a {@code registrationDate} later than the server
 * date.
 */
public class FutureRegistrationDateExceptionHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", ex.getMessage())));
    }
}
