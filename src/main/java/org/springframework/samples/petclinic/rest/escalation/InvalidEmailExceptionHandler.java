package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidEmailException;

/**
 * Responds 400 when an owner request supplies an {@code email} that is not a syntactically valid
 * address.
 */
public class InvalidEmailExceptionHandler {

    public void handle(@Parameter InvalidEmailException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", ex.getMessage())));
    }
}
