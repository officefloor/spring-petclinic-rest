package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a {@link DisposableEmailDomainException}: the owner request carried an
 * {@code email} whose domain is on the disposable-domain blocklist. The body is an
 * RFC7807 {@code application/problem+json} document.
 */
public class DisposableEmailDomainHandler {

    public void handle(@Parameter DisposableEmailDomainException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetails.send(response, ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
