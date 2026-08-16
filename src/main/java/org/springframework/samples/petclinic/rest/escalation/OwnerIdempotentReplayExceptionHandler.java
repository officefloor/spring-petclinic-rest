package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Responds 200 with the originally created owner when a create repeats an already-seen
 * {@code Idempotency-Key}, so the repeat is a safe no-op rather than a duplicate or a 409.
 */
public class OwnerIdempotentReplayExceptionHandler {

    public void handle(@Parameter OwnerIdempotentReplayException ex,
            ObjectResponse<ResponseEntity<OwnerDto>> response) {
        response.send(ResponseEntity.ok(ex.getOwner()));
    }
}
