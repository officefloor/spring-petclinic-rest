package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Membership;

/**
 * Step of {@code POST /api/owners} that emits an audit record for a successful create. Runs after
 * {@link SaveOwner} (so the owner's generated id is available) and before
 * {@link RespondWithOwnerCreated}.
 *
 * <p>Publishes two things to the dedicated {@code AUDIT} logger:
 * <ul>
 * <li>the human-readable audit line carrying the owner id, the assigned {@code memberId}, the
 * stored (business-day-adjusted) {@code registrationDate} and the numeric {@code membershipLevel}; and
 * <li>an immutable structured {@link OwnerCreatedEvent} as a JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}, where {@code seq} is a
 * monotonically increasing integer across creates and the identifier is the owner's primary
 * identifier, the {@code memberId}.
 * </ul>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence across creates, process-wide. */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    public void service(@Val Owner owner) {
        int membershipLevel = owner.getMembershipLevel() != null ? owner.getMembershipLevel()
                : Membership.levelOf(owner);

        AUDIT.info(
                "Owner created id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifierOf(owner), membershipLevel);
        AUDIT.info(event.toJson());
    }

    /** The owner's primary identifier carried by the structured event: the {@code memberId}. */
    private static String primaryIdentifierOf(Owner owner) {
        return owner.getMemberId();
    }
}
