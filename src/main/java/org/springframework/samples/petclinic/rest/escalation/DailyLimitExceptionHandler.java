package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DailyLimitExceptionHandler {

    public void handle(@Parameter DailyLimitException ex,
            ObjectResponse<ResponseEntity<Void>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
    }
}
