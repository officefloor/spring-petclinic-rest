package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["household"]}} when the create-owner
 * lastName and address already belong to another owner and {@code sharesHousehold} was
 * not set true.
 */
public class OwnerHouseholdDuplicateExceptionHandler {

    public void handle(@Parameter OwnerHouseholdDuplicateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("household"))));
    }
}
