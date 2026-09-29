package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 as an RFC7807 {@code application/problem+json} body when a create/update-owner
 * postcode is out of range for the owner's city region. The {@code errors} array names the offending
 * field.
 */
public class PostcodeOutOfRangeExceptionHandler {

    public void handle(@Parameter PostcodeOutOfRangeException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The supplied postcode is out of range for the owner's city region");
        detail.setProperty("errors", List.of("postcode"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
