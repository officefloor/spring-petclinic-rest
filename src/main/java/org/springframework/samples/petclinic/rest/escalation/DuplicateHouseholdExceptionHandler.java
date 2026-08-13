package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["lastName", "address"]}} when the
 * create-owner request shares a household (same last name and address) with an existing
 * owner and did not opt in via {@code sharesHousehold: true}.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("lastName", "address"))));
    }
}
