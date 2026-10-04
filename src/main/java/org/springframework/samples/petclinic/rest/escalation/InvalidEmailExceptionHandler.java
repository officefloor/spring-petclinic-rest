package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.InvalidEmailException;

/**
 * Responds 400 with an RFC 7807 {@code application/problem+json} body when an owner create
 * request supplies an {@code email} that is not a syntactically valid address. The
 * {@code errors} array names {@code email}.
 */
public class InvalidEmailExceptionHandler {

    public void handle(@Parameter InvalidEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied email address is not valid", List.of("email"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
