package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DisposableEmailDomainExceptionHandler {

    public void handle(@Parameter DisposableEmailDomainException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()));
    }
}
