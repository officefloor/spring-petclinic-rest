package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class MissingRequiredFieldsExceptionHandler {

    public void handle(@Parameter MissingRequiredFieldsException ex,
            ObjectResponse<ResponseEntity<RequiredFieldsErrorResponse>> response) {
        response.send(ResponseEntity.badRequest().body(new RequiredFieldsErrorResponse(ex.getFields())));
    }
}
