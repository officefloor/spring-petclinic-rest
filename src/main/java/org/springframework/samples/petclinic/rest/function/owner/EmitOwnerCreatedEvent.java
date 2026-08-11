package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.support.OwnerEventSequence;

/**
 * Emits the immutable structured {@link OwnerCreatedEvent} to the dedicated {@code AUDIT} logger on
 * successful create, in addition to the human-readable line from {@link AuditOwnerCreated}. Draws
 * the monotonically increasing {@code seq} from {@link OwnerEventSequence} and captures the owner's
 * current primary identifier and membership level. Runs after {@link SaveOwner} (so the id exists),
 * alongside {@link AuditOwnerCreated}.
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerEventSequence sequence) {
        OwnerCreatedEvent event = OwnerCreatedEvent.from(sequence.next(), owner);
        AUDIT.info("{}", event.toJson());
    }
}
