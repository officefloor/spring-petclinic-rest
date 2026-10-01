package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body whose {@code errors} array lists the name of each required
 * owner field that was missing or blank.
 */
public class OwnerFieldsMissingExceptionHandler {

    public void handle(@Parameter OwnerFieldsMissingException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", ex.getFields());
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }
}
