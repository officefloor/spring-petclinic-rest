package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class OwnerCityFullExceptionHandler {

    public void handle(@Parameter OwnerCityFullException ex,
            ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }
}
