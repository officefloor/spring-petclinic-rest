package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 (Too Many Requests) when a create request is rejected because the
 * maximum number of owners have already been registered today.
 */
public class DailyOwnerLimitExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("errors", List.of("registrationDate"))));
    }
}
