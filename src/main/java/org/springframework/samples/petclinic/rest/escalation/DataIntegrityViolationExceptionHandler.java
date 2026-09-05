package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public class DataIntegrityViolationExceptionHandler {

    public void handle(@Parameter DataIntegrityViolationException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.NOT_FOUND,
                "The requested resource could not be processed due to a data constraint violation");
        ProblemDetails.send(response, HttpStatus.NOT_FOUND, detail);
    }
}
