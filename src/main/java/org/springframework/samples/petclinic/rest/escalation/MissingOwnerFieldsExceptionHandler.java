package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request with missing or blank required fields, with a JSON
 * body whose 'errors' array lists the name of each such field.
 */
public class MissingOwnerFieldsExceptionHandler {

    public void handle(@Parameter MissingOwnerFieldsException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", ex.getFields())));
    }
}
