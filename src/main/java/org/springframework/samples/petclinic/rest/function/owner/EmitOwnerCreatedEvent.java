package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;

import tools.jackson.databind.ObjectMapper;

/**
 * Emits the immutable {@link OwnerCreatedEvent} as a single JSON line on the {@code AUDIT} logger, in
 * addition to the human-readable line from {@link AuditOwnerCreated}. Runs after {@link SaveOwner} so
 * the owner id is assigned, and draws its sequence number from {@link AuditSequence} so events are
 * totally ordered across creates.
 */
public class EmitOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final ObjectMapper JSON = new ObjectMapper();

    public void service(@Val Owner owner, AuditSequence sequence) {
        OwnerCreatedEvent event = OwnerCreatedEvent.of(sequence.next(), owner);
        AUDIT.info(JSON.writeValueAsString(event));
    }
}
