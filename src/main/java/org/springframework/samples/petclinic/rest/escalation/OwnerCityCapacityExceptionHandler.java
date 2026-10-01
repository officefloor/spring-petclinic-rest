package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names {@code city} when the owner being
 * created belongs to a city that already contains the maximum number of owners.
 */
public class OwnerCityCapacityExceptionHandler {

    public void handle(@Parameter OwnerCityCapacityException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("city"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }
}
