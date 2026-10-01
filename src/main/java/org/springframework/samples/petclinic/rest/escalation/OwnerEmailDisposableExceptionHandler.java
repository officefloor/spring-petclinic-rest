package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with an RFC7807 {@code application/problem+json} body when the submitted
 * email domain is on the disposable-domain blocklist.
 */
public class OwnerEmailDisposableExceptionHandler {

    public void handle(@Parameter OwnerEmailDisposableException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        response.send(ProblemDetails.response(ex, HttpStatus.BAD_REQUEST,
                "The submitted email uses a disposable domain that is not accepted"));
    }
}
