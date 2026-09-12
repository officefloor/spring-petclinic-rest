package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Emits, on the dedicated {@code AUDIT} logger, the side-effects of a successfully
 * created owner. Two things are logged:
 * <ol>
 * <li>a human-readable audit line carrying the owner id, memberId,
 * registrationDate and membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} rendered as JSON
 * ({@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}),
 * whose {@code seq} increases monotonically across all creates.</li>
 * </ol>
 * Runs after {@link SaveOwner} so the generated id is available.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence shared across all owner creates. */
    private static final AtomicLong SEQ = new AtomicLong();

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(),
                MembershipLevel.of(owner));

        audit.info(OwnerCreatedEvent.of(SEQ.incrementAndGet(), owner).toJson());
    }
}
