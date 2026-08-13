package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names the offending
 * {@code city} field when a create request targets a city that already contains the
 * maximum number of owners.
 */
public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("city"))));
    }
}
