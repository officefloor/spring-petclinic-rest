package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Loads the owner an already-seen {@code Idempotency-Key} first created, given its id from
 * the {@link CheckIdempotencyKey} replay branch, and publishes it for the shared
 * {@link RespondWithOwner} responder to return with 200.
 */
public class LoadReplayedOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId));
    }
}
