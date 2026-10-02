package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a new owner would share a household (same last name and address)
 * with an existing owner and the request did not set {@code sharesHousehold = true}.
 */
public class DuplicateHouseholdExceptionHandler {

    public void handle(@Parameter DuplicateHouseholdException ex,
            ObjectResponse<ResponseEntity<Void>> response) {
        response.send(new ResponseEntity<>(HttpStatus.CONFLICT));
    }
}
