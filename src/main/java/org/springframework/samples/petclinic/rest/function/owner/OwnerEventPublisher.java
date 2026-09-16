package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevel;

import tools.jackson.databind.ObjectMapper;

/**
 * Publishes immutable, structured owner lifecycle events as JSON to the dedicated
 * {@code AUDIT} logger. Owns the monotonically increasing sequence shared across creates,
 * so each emitted event carries a unique, ordered {@code seq}.
 *
 * <p>Separate from {@link AuditOwnerCreated} (the human-readable audit line): this bean
 * carries the machine-readable event and the sequence state, keeping each concern in its
 * own small unit.
 */
@Component
public class OwnerEventPublisher {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AtomicLong sequence = new AtomicLong();

    private final ObjectMapper objectMapper;

    public OwnerEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Emit an {@code OWNER_CREATED} event for a freshly created (and saved) owner. */
    public void ownerCreated(Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(this.sequence.incrementAndGet(),
                owner.getId(), owner.getPrimaryIdentifier(), MembershipLevel.of(owner),
                OwnerCreatedEvent.EVENT_TYPE);
        AUDIT.info(this.objectMapper.writeValueAsString(event));
    }
}
