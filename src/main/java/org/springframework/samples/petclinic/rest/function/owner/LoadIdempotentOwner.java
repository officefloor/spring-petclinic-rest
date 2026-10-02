package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.NotFoundException;
import org.springframework.samples.petclinic.rest.function.common.Lookups;

/**
 * Replay step: loads the owner a previously seen {@code Idempotency-Key} first created (its id arrives as
 * the {@code @Parameter} from {@link CheckIdempotency}'s replay flow) so {@link RespondWithOwner} can
 * return it with 200. Runs under a read-only transaction, like the GET pipeline, so the response mapping
 * sees a managed entity.
 */
public class LoadIdempotentOwner {

    public void service(@Parameter Integer ownerId, OwnerRepository ownerRepository, Out<Owner> loaded)
            throws NotFoundException {
        loaded.set(Lookups.findOrNotFound(() -> ownerRepository.findById(ownerId),
                "Owner not found: " + ownerId));
    }
}
