package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DuplicateTelephoneExceptionHandler {

    public void handle(@Parameter DuplicateTelephoneException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", Map.of("telephone", ex.getMessage()))));
    }
}
