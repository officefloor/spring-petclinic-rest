package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a create request's identity key already belongs to an existing,
 * non-deleted owner.
 */
public class OwnerDuplicateIdentityExceptionHandler {

    public void handle(@Parameter OwnerDuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT));
    }
}
