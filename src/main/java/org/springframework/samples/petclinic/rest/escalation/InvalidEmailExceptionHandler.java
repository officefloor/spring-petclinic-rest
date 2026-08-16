package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request whose 'email', though present, is not a syntactically
 * valid address, with an RFC7807 application/problem+json body whose 'errors' array names 'email'.
 */
public class InvalidEmailExceptionHandler {

    public void handle(@Parameter InvalidEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The email address is not a syntactically valid address");
        detail.setProperty("errors", List.of("email"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
