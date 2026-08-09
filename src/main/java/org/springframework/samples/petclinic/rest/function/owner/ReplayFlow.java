package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The "replay" branch out of {@link CheckIdempotencyKey}: the idempotency key was already seen, so
 * hand the originally created owner to {@link RespondWithExistingOwner} (which answers 200) instead
 * of creating a duplicate. The owner is passed as the flow argument and arrives as {@code @Parameter}.
 */
@FunctionalInterface
public interface ReplayFlow {

    void replay(Owner owner);
}
