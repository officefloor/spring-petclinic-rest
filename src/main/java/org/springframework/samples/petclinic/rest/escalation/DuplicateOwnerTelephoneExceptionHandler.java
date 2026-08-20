package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DuplicateOwnerTelephoneExceptionHandler {

    public void handle(@Parameter DuplicateOwnerTelephoneException ex,
            ObjectResponse<ResponseEntity<OwnerValidationErrors>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new OwnerValidationErrors(List.of("telephone"))));
    }
}
