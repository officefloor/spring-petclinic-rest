package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the structured {@code OWNER_CREATED} audit event for a created owner. Runs after
 * {@link SaveOwner} (so the owner has its generated id) and after {@link AuditOwnerCreated}
 * (the human-readable line), delegating to {@link OwnerEventPublisher} to assign the
 * sequence and log the JSON event.
 */
public class EmitOwnerCreatedEvent {

    public void service(@Val Owner owner, OwnerEventPublisher publisher) {
        publisher.ownerCreated(owner);
    }
}
