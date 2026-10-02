package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 Too Many Requests when 100 or more owners have already been created today.
 */
public class DailyOwnerLimitExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitException ex,
            ObjectResponse<ResponseEntity<Void>> response) {
        response.send(new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS));
    }
}
