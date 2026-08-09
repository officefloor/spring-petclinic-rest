package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["household"]}} when the create request would
 * duplicate another owner's household (same lastName and address) without opting in via
 * {@code sharesHousehold}.
 */
public class OwnerHouseholdConflictExceptionHandler {

    public void handle(@Parameter OwnerHouseholdConflictException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("household"))));
    }
}
