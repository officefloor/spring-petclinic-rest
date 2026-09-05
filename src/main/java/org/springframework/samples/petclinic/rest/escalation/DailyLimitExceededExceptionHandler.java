package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DailyLimitExceededException;

/**
 * Responds 429 when 100 or more owners have already been created today.
 */
public class DailyLimitExceededExceptionHandler {

    public void handle(@Parameter DailyLimitExceededException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("errors", ex.getMessage())));
    }
}
