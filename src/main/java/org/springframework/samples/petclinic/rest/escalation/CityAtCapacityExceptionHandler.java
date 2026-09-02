package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CityAtCapacityExceptionHandler {

    public void handle(@Parameter CityAtCapacityException ex,
            ObjectResponse<ResponseEntity<Object>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ProblemDetails.build(ex, HttpStatus.CONFLICT, ex.getMessage())));
    }
}
