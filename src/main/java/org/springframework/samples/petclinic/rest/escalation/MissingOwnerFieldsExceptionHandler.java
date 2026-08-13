package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 with a JSON body {@code {"errors": [...]}} listing the required owner
 * fields that were missing or blank.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", ex.getFields())));
    }
}
