package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class InvalidOwnerEmailExceptionHandler {

    public void handle(@Parameter InvalidOwnerEmailException ex,
            ObjectResponse<ResponseEntity<OwnerValidationErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerValidationErrors(List.of("email"))));
    }
}
