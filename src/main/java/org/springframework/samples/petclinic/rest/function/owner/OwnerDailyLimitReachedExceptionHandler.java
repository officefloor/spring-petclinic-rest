package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 Too Many Requests when 100 or more owners already carry the current day's
 * registration date.
 */
public class OwnerDailyLimitReachedExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitReachedException ex,
            ObjectResponse<ResponseEntity<String>> response) {
        response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS));
    }
}
