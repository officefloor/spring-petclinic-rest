package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Loads the owner originally created for a repeated {@code Idempotency-Key}, by the id
 * {@link CheckIdempotency} passed on the {@code existing} branch, and publishes it for
 * {@link RespondWithExistingOwner}.
 */
public class LoadExistingOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded) {
        loaded.set(ownerRepository.findById(ownerId));
    }
}
