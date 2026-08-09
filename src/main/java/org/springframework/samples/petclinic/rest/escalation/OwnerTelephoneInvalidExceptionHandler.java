package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body {@code {"errors": ["telephone"]}} when the supplied
 * telephone is not exactly ten digits once non-digit characters are removed.
 */
public class OwnerTelephoneInvalidExceptionHandler {

    public void handle(@Parameter OwnerTelephoneInvalidException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", java.util.List.of("telephone"))));
    }
}
