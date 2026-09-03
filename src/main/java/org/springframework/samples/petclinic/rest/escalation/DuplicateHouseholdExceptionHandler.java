package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 when a create-owner request would share a household (same lastName and address) with
 * an existing owner without opting in via {@code sharesHousehold: true}.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(new ResponseEntity<>(Map.of("errors", List.of("lastName", "address")), HttpStatus.CONFLICT));
    }
}
