package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class InvalidPostcodeExceptionHandler {

    public void handle(@Parameter InvalidPostcodeException ex,
            ObjectResponse<ResponseEntity<FieldErrorsResponse>> response) {
        response.send(ResponseEntity.badRequest().body(new FieldErrorsResponse(List.of("postcode"))));
    }
}
