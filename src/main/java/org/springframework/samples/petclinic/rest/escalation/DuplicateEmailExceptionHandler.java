package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 to a create-owner request whose lower-cased email is already used by
 * another owner, with a JSON body whose 'errors' array names 'email'.
 */
public class DuplicateEmailExceptionHandler {

    public void handle(@Parameter DuplicateEmailException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("email"))));
    }
}
