package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 409 with a JSON body {@code {"errors": ["telephone"]}} when the supplied telephone,
 * once normalized, is already used by another owner.
 */
public class OwnerTelephoneConflictExceptionHandler {

    public void handle(@Parameter OwnerTelephoneConflictException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("errors", List.of("telephone"))));
    }
}
