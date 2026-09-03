package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 when a create-owner request's lower-cased email is already used by an existing owner.
 */
public class DuplicateEmailExceptionHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(new ResponseEntity<>(Map.of("errors", List.of("email")), HttpStatus.CONFLICT));
    }
}
