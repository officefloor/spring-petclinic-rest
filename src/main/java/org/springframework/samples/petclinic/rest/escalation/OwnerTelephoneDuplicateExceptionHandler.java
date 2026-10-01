package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names {@code telephone} when the
 * normalized telephone being created is already used by another owner.
 */
public class OwnerTelephoneDuplicateExceptionHandler {

    public void handle(@Parameter OwnerTelephoneDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("telephone"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }
}
