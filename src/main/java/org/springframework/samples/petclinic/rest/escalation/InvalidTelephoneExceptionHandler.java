package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when a create-owner request's telephone is not exactly 10 digits after non-digit
 * characters are stripped.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", List.of("telephone"))));
    }
}
