package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the originally created owner with 200 when a create repeats an already-seen
 * {@code Idempotency-Key}. The body is the DTO exactly as first sent (only the status differs
 * from the original 201).
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, ObjectResponse<OwnerDto> response) {
        response.send(ex.getOwner());
    }
}
