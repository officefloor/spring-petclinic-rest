package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that records a successful create on the dedicated {@code AUDIT}
 * logger. Runs after {@link SaveOwner} so the owner has its persisted id, and emits two things:
 * <ul>
 * <li>a human-readable audit line carrying the owner id, the assigned {@code memberId}, the
 * effective {@code registrationDate} and the assigned {@code membershipLevel}; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} rendered as JSON, stamped with a
 * process-wide {@code seq} that increases monotonically across every create.</li>
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence stamped onto each structured event, across all creates. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        AUDIT.info("owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                owner.getMembershipLevel());

        OwnerCreatedEvent event = OwnerCreatedEvent.forOwner(SEQUENCE.incrementAndGet(), owner);
        AUDIT.info(event.toJson());
    }
}
