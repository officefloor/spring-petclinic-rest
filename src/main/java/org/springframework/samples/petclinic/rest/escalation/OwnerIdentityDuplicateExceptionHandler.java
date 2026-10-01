package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names {@code identityKey} when the owner
 * being created has the same derived {@code identityKey} as an existing owner.
 */
public class OwnerIdentityDuplicateExceptionHandler {

    public void handle(@Parameter OwnerIdentityDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("identityKey"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }
}
