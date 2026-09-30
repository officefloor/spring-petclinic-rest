package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Loads an owner by an id received as the previous step's return value, mirroring {@link LoadOwner}
 * (which loads from a path variable). Used by the idempotent-create replay branch, which carries the
 * originally created owner's id as a {@code @Parameter} and then reuses {@link RespondWithOwner}.
 */
public class LoadOwnerById {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId), "Owner not found: " + ownerId));
    }
}
