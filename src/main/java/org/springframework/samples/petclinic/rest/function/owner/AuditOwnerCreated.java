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
 * <li>the human-readable audit line carrying the owner id, the assigned {@code customerCode}, the
 * stored (business-day-adjusted) {@code registrationDate}, the numeric {@code membershipLevel} and the
 * assigned {@code membershipNumber}; and
 * <li>an immutable structured {@link OwnerCreatedEvent} as a JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}, where {@code seq} is a
 * monotonically increasing integer across creates and the identifier is the owner's current primary
 * identifier (the {@code customerCode} today; the {@code memberId} once the two are unified).
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
                "Owner created id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(), membershipLevel,
                owner.getMembershipNumber());

        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifierOf(owner), membershipLevel);
        AUDIT.info(event.toJson());
    }

    /**
     * The owner's current primary identifier carried by the structured event. Today this is the
     * {@code customerCode}; when the {@code customerCode} is unified into the {@code memberId}, this
     * single accessor becomes the place that yields the {@code memberId} instead.
     */
    private static String primaryIdentifierOf(Owner owner) {
        return owner.getCustomerCode();
    }
}
