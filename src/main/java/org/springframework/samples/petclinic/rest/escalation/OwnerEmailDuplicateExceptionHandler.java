package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names {@code email} when the lower-cased
 * email being created is already used by another owner.
 */
public class OwnerEmailDuplicateExceptionHandler {

    public void handle(@Parameter OwnerEmailDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("email"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }
}
