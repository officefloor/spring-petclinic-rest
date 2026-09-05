package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateTelephoneException;

/**
 * Responds 409 when a create-owner request's normalized telephone is already used by another owner.
 */
public class DuplicateTelephoneExceptionHandler {

    public void handle(@Parameter DuplicateTelephoneException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", ex.getMessage())));
    }
}
