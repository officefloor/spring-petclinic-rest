package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a create-owner request whose lastName and address already match an existing
 * owner's household, with a JSON body whose 'errors' array names 'lastName' and 'address'.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("lastName", "address"))));
    }
}
