package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class OwnerFieldsInvalidExceptionHandler {

    public void handle(@Parameter OwnerFieldsInvalidException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The request contains invalid or missing owner fields");
        detail.setProperty("errors", ex.getErrors());
        response.send(ProblemDetails.response(HttpStatus.BAD_REQUEST, detail));
    }
}
