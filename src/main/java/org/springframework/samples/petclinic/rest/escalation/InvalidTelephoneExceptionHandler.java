package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request whose telephone is not exactly 10 digits after
 * stripping non-digit characters, with a JSON body whose 'errors' array names 'telephone'.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", java.util.List.of("telephone"))));
    }
}
