package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DisposableEmailDomainException;

/**
 * Responds 400 when a request supplies an email whose domain is a known disposable-email
 * provider.
 */
public class DisposableEmailDomainExceptionHandler {

    public void handle(@Parameter DisposableEmailDomainException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.BAD_REQUEST,
                "The email domain must not be a disposable-email provider "
                        + "(mailinator.com, tempmail.com, guerrillamail.com)");
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(detail));
    }
}
