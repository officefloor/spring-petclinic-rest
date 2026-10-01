package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a create request's normalized last name and address already belong to
 * another owner and the request did not opt in with {@code sharesHousehold: true}.
 */
public class OwnerHouseholdDuplicateExceptionHandler {

    public void handle(@Parameter OwnerHouseholdDuplicateException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT));
    }
}
