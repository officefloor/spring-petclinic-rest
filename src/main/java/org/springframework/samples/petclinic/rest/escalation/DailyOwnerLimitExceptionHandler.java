package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DailyOwnerLimitExceptionHandler {

    public void handle(@Parameter DailyOwnerLimitException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(new ResponseEntity<>(Map.of("errors", List.of("dailyLimit")),
                HttpStatus.TOO_MANY_REQUESTS));
    }
}
