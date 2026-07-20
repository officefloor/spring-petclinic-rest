package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

public class NotFoundExceptionHandler {

    public void handle(@Parameter NotFoundException ex, ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.notFound().build());
    }
}
