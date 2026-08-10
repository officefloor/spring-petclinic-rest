package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body {@code {"errors": ["postcode"]}} when the supplied postcode is
 * malformed or out of range for the owner's city.
 */
public class OwnerPostcodeInvalidExceptionHandler {

    public void handle(@Parameter OwnerPostcodeInvalidException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", java.util.List.of("postcode"))));
    }
}
