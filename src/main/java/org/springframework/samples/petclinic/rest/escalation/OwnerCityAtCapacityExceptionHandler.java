package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class OwnerCityAtCapacityExceptionHandler {

    public void handle(@Parameter OwnerCityAtCapacityException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.CONFLICT, ex.getMessage());
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).body(detail));
    }
}
