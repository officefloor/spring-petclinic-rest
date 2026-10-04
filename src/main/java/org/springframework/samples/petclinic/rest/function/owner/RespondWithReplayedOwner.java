package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Replays the owner originally created for a repeated {@code Idempotency-Key}, responding 200 with
 * the stored representation. Reached only from {@link CheckIdempotencyKey}'s {@code replay} branch,
 * which supplies the remembered {@link OwnerDto} as the parameter.
 */
public class RespondWithReplayedOwner {

    public void service(@Parameter OwnerDto owner, ObjectResponse<OwnerDto> response) {
        response.send(owner);
    }
}
