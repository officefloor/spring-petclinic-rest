package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 to a create-owner request whose supplied registrationDate is later than the server
 * date, with a JSON body whose 'errors' array names 'registrationDate'.
 */
public class FutureRegistrationDateExceptionHandler {

    public void handle(@Parameter FutureRegistrationDateException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", List.of("registrationDate"))));
    }
}
