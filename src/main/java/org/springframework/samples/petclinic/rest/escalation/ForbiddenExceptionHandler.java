package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;

/**
 * Maps a YAML {@code authorize:} denial to 403, mirroring how Spring's own
 * ExceptionTranslationFilter would translate an AccessDeniedException on the servlet
 * filter chain - required because OfficeFloor evaluates {@code authorize:} inline in the
 * function pipeline rather than via that filter.
 */
public class ForbiddenExceptionHandler {

    public void handle(@Parameter AuthorizationDeniedException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail detail = ProblemDetails.build(ex, HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource");
        response.send(ResponseEntity.status(HttpStatus.FORBIDDEN).body(detail));
    }
}
