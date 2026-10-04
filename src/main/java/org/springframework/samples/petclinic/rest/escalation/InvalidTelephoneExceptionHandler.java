package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidTelephoneException;

/**
 * Responds 400 with an RFC 7807 {@code application/problem+json} body when an owner create
 * request's telephone is not exactly ten digits after stripping non-digit characters. The
 * {@code errors} array names {@code telephone}.
 */
public class InvalidTelephoneExceptionHandler {

    public void handle(@Parameter InvalidTelephoneException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied telephone number is not valid", List.of("telephone"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
