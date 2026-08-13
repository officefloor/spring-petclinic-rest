package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 with a JSON body {@code {"errors": ["dailyLimit"]}} when 100 or more
 * owners have already been created today.
 */
public class DailyLimitExceptionHandler {

    public void handle(@Parameter DailyLimitException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("errors", List.of("dailyLimit"))));
    }
}
