package org.springframework.samples.petclinic.rest.function.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import tools.jackson.databind.ObjectMapper;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier;
import org.springframework.samples.petclinic.model.OwnerSegment;

/**
 * Emits the immutable structured {@link OwnerCreatedEvent} (schema version 2) to the {@code AUDIT}
 * log as JSON, in addition to the human-readable line written by {@link AuditOwnerCreated}. The
 * event carries the owner's current primary identifier (see {@link OwnerPrimaryIdentifier}) and its
 * marketing segment (see {@link OwnerSegment}) recomputed from the version-2 owner.
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    public void service(@Val Owner owner, OwnerCreatedEventSequence sequence) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(sequence.next(), owner.getId(),
                OwnerPrimaryIdentifier.of(owner), MembershipLevel.effective(owner),
                OwnerSegment.forOwner(owner));
        AUDIT.info("{}", JSON.writeValueAsString(event));
    }
}
