package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DuplicateEmailException;

/**
 * Responds 409 when a create-owner request's lower-cased email is already used by another owner.
 */
public class DuplicateEmailExceptionHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", ex.getMessage())));
    }
}
