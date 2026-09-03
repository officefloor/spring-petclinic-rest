package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;

/**
 * Responds 400 when an owner request carries an {@code email} whose domain is on the disposable-domain
 * blocklist.
 */
public class DisposableEmailExceptionHandler {

    public void handle(@Parameter DisposableEmailException ex,
            ObjectResponse<ResponseEntity<Map<String, List<String>>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", List.of("email"))));
    }
}
