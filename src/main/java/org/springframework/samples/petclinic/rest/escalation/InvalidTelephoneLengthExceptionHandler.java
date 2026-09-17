package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class InvalidTelephoneLengthExceptionHandler {

    public void handle(@Parameter InvalidTelephoneLengthException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(ResponseEntity.badRequest().body(ex.getMessage()));
    }
}
