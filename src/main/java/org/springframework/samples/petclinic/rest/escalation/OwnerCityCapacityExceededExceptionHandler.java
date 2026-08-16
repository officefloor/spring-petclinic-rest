package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["city"]}} when the create-owner city
 * already contains 50 or more owners.
 */
public class OwnerCityCapacityExceededExceptionHandler {

    public void handle(@Parameter OwnerCityCapacityExceededException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("city"))));
    }
}
