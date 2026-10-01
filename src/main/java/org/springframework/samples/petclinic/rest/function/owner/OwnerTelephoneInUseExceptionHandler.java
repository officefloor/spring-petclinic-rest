package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a create request's normalized telephone is already used by another
 * owner.
 */
public class OwnerTelephoneInUseExceptionHandler {

    public void handle(@Parameter OwnerTelephoneInUseException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT));
    }
}
