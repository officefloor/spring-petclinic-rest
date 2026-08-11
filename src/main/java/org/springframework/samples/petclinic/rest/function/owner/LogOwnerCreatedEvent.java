package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.mapper.MembershipLevel;
import org.springframework.samples.petclinic.mapper.OwnerSegment;
import org.springframework.samples.petclinic.mapper.PrimaryIdentifier;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits the immutable {@link OwnerCreatedEvent} to the dedicated {@code AUDIT} logger, in addition to
 * the human-readable audit line written by {@link LogOwnerAudit}. Runs after the owner has been saved
 * (so its generated id is available) and before the response is sent.
 *
 * <p>The event's {@code seq} is a process-wide monotonically increasing integer across creates, and
 * its identifier is the owner's current primary identifier via {@link PrimaryIdentifier} &mdash; the
 * unified {@code memberId}.
 */
public class LogOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing across creates for the lifetime of the process. */
    private static final AtomicInteger SEQ = new AtomicInteger();

    public void service(@Val Owner owner) {
        OwnerCreatedEvent event = new OwnerCreatedEvent(OwnerCreatedEvent.SCHEMA_VERSION,
                SEQ.incrementAndGet(), owner.getId(), PrimaryIdentifier.of(owner),
                MembershipLevel.of(owner), OwnerSegment.of(owner), OwnerCreatedEvent.OWNER_CREATED);
        AUDIT.info(event.toJson());
    }
}
