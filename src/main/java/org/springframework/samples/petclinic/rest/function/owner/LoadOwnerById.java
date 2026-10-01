package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Loads an owner by an id carried as a {@code @Parameter} (rather than a path variable) and publishes
 * it as {@code Out<Owner>}. Used by the idempotency replay branch to fetch the originally created
 * owner before the shared {@link RespondWithOwner} responder returns it with 200.
 */
public class LoadOwnerById {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId), "Owner not found: " + ownerId));
    }
}
