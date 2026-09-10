package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;

/**
 * Emits an audit trail entry, via the dedicated {@code AUDIT} logger, for a
 * successfully created owner. The line carries the owner id, the assigned
 * memberId, the registrationDate and the assigned membershipLevel.
 *
 * <p>In addition to that human-readable line, an immutable {@link OwnerCreatedEvent} is
 * emitted as a compact JSON object on the same {@code AUDIT} logger, giving the trail a
 * machine-readable record carrying a monotonic {@code seq}, the owner id, the owner's
 * memberId and the membership level.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence shared by every owner-created event. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevels.of(owner));
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                owner.getMemberId(), MembershipLevels.of(owner), OwnerCreatedEvent.OWNER_CREATED);
        audit.info(event.toJson());
    }
}
