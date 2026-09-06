package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        response.send(ResponseEntity.ok(ex.getOwner()));
    }
}
