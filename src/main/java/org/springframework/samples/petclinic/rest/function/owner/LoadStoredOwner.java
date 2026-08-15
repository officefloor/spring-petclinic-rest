package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Replay step of {@code POST /api/owners}: loads the owner originally created under a repeated
 * {@code Idempotency-Key} (its id arriving from {@link CheckIdempotencyKey}) so {@link
 * RespondWithOwner} can return it with 200.
 */
public class LoadStoredOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId));
    }
}
