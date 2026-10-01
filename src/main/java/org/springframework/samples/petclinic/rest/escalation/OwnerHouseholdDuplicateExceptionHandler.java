package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body whose {@code errors} array names {@code householdId} when the owner
 * being created would be an undeclared second member of an existing household.
 */
public class OwnerHouseholdDuplicateExceptionHandler {

    public void handle(@Parameter OwnerHouseholdDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        Map<String, Object> body = Map.of("errors", List.of("householdId"));
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
    }
}
