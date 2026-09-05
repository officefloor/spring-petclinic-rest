package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a {@link DuplicateEmailException}: the create-owner lower-cased email
 * is already used by another owner.
 */
public class DuplicateEmailHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT));
    }
}
