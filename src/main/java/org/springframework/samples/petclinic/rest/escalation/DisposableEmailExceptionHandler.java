package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DisposableEmailException;

/**
 * Responds 400 with an RFC 7807 {@code application/problem+json} body when an owner create
 * request supplies an {@code email} whose domain is on the disposable-email blocklist. The
 * {@code errors} array names {@code email}.
 */
public class DisposableEmailExceptionHandler {

    public void handle(@Parameter DisposableEmailException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "Disposable email addresses are not accepted", List.of("email"));
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
