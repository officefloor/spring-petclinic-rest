package org.springframework.samples.petclinic.rest.escalation;

import java.util.Map;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.function.owner.DisposableEmailDomainException;

/**
 * Responds 400 when an owner request supplies an {@code email} whose domain is on the
 * disposable-domain blocklist.
 */
public class DisposableEmailDomainExceptionHandler {

    public void handle(@Parameter DisposableEmailDomainException ex,
            ObjectResponse<ResponseEntity<Map<String, Object>>> response) {
        response.send(ResponseEntity.badRequest().body(Map.of("errors", ex.getMessage())));
    }
}
