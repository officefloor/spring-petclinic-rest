package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.event.OwnerCreatedEvent;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.MembershipPoints;
import org.springframework.samples.petclinic.util.PrimaryIdentifier;

/**
 * Emits the immutable {@link OwnerCreatedEvent} to the {@code AUDIT} log after
 * {@link AuditOwnerCreated} has written the human-readable line. Runs once the owner is persisted, so
 * it carries the generated id, the current primary identifier and the derived membership level. Each
 * event takes the next value from a process-wide monotonic sequence so consumers can order creates
 * and detect gaps.
 */
public class AuditOwnerCreatedEvent {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Process-wide monotonic sequence shared across all owner creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        Integer membershipLevel = MembershipLevel.forPoints(MembershipPoints.of(owner));
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(),
                owner.getId(), PrimaryIdentifier.of(owner), membershipLevel);
        AUDIT.info("{}", event.toJson());
    }
}
