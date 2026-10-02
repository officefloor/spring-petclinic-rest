package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 Conflict when a new owner's whole identity key equals that of an existing owner.
 */
public class DuplicateIdentityExceptionHandler {

    public void handle(@Parameter DuplicateIdentityException ex,
            ObjectResponse<ResponseEntity<Void>> response) {
        response.send(new ResponseEntity<>(HttpStatus.CONFLICT));
    }
}
