package org.springframework.samples.petclinic.rest.escalation;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the originally created owner with 200 when a create-owner request repeats an already-seen
 * {@code Idempotency-Key}, instead of the 409 a genuine duplicate would get.
 */
public class IdempotentReplayExceptionHandler {

    public void handle(@Parameter IdempotentReplayException ex, ObjectResponse<OwnerDto> response) {
        response.send(ex.getOwner());
    }
}
