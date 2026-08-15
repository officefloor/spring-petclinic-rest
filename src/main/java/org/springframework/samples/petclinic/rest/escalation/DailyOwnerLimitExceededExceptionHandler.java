package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 429 (Too Many Requests) to a create-owner request made once the day's owner
 * registration limit has been reached, with a JSON body whose 'errors' array names
 * 'registrationDate'.
 */
public class DailyOwnerLimitExceededExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitExceededException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("errors", List.of("registrationDate"))));
    }
}
