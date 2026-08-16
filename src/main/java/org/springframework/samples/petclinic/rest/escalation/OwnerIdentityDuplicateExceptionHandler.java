package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["identityKey"]}} when a new owner's whole
 * derived {@code identityKey} matches an existing owner's.
 */
public class OwnerIdentityDuplicateExceptionHandler {

    public void handle(@Parameter OwnerIdentityDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("identityKey"))));
    }
}
