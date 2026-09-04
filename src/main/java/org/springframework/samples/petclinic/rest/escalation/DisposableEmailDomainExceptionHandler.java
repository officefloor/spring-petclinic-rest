package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DisposableEmailDomainExceptionHandler {

    /** Response body: {@code {"error": "..."}}. */
    public record DisposableEmailDomain(String error) {
    }

    public void handle(@Parameter DisposableEmailDomainException ex,
            ObjectResponse<ResponseEntity<DisposableEmailDomain>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DisposableEmailDomain(ex.getMessage())));
    }
}
