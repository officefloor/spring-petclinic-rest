package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Loads the originally created {@link Owner} for an idempotent replay, keyed by the id
 * {@link CheckIdempotencyKey} recovered from the {@link IdempotencyStore}, and publishes it for
 * {@link RespondWithOwner} to return with 200.
 */
public class LoadOwnerById {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId), "Owner not found: " + ownerId));
    }
}
