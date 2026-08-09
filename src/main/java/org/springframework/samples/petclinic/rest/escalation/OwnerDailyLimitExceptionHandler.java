package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 Too Many Requests with a JSON body {@code {"errors": ["registrationDate"]}} when the
 * create request would exceed the daily limit of 100 owners created today.
 */
public class OwnerDailyLimitExceptionHandler {

    public void handle(@Parameter OwnerDailyLimitException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("errors", List.of("registrationDate"))));
    }
}
