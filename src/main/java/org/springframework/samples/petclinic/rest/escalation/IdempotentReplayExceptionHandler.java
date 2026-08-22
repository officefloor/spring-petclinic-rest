package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Handles an {@link IdempotentReplayException} by responding {@code 200 OK} with the originally
 * created owner, so a repeated create with an already-seen {@code Idempotency-Key} returns the
 * first result rather than creating a duplicate.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, ObjectResponse<ResponseEntity<OwnerDto>> response) {
        response.send(ResponseEntity.ok(ex.getOwner()));
    }
}
