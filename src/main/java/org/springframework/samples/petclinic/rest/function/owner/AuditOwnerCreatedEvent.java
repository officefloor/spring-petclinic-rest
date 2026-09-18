package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: emits the immutable structured create event to the
 * {@code AUDIT} logger. Runs after {@link AuditOwnerCreated} (the human-readable line) and
 * after {@link SaveOwner} so the owner carries its persisted id. The monotonic sequence and
 * JSON shape live in {@link OwnerCreatedEventPublisher}.
 */
public class AuditOwnerCreatedEvent {

    public void service(@Val Owner owner, OwnerCreatedEventPublisher publisher) {
        publisher.publish(owner);
    }
}
