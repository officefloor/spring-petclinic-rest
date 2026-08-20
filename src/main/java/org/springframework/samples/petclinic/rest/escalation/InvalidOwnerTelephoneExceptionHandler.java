package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class InvalidOwnerTelephoneExceptionHandler {

    public void handle(@Parameter InvalidOwnerTelephoneException ex,
            ObjectResponse<ResponseEntity<OwnerValidationErrors>> response) {
        response.send(ResponseEntity.badRequest().body(new OwnerValidationErrors(List.of("telephone"))));
    }
}
