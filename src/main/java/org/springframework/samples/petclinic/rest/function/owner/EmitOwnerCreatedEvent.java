package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits the immutable {@link OwnerCreatedEvent} as a structured JSON line on the dedicated
 * {@code AUDIT} logger, in addition to the human-readable line from {@link AuditOwnerCreated}. Runs
 * after {@link SaveOwner} so the owner has an id.
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        AUDIT.info(OwnerCreatedEvent.forOwner(owner, ownerRepository).toJson());
    }
}
