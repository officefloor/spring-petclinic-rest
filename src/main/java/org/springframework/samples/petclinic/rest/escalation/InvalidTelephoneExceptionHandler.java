package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body of the form {@code {"errors": ["telephone"]}} when the
 * supplied telephone is not exactly 10 digits after stripping non-digit characters.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", List.of("telephone"))));
    }
}
