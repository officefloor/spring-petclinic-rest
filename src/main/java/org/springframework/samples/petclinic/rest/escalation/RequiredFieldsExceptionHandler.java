package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RequiredFieldsExceptionHandler {

    public void handle(@Parameter RequiredFieldsException ex,
            ObjectResponse<ResponseEntity<ValidationErrorsResponse>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorsResponse(ex.getErrors())));
    }
}
