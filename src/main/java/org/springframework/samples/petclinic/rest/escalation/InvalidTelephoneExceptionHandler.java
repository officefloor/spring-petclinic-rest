package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request whose telephone cannot be converted to valid E.164
 * form, with an RFC7807 application/problem+json body whose 'errors' array names 'telephone'.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The telephone number cannot be converted to a valid E.164 form");
        detail.setProperty("errors", List.of("telephone"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
