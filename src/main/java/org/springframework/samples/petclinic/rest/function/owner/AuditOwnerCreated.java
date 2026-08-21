package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits the audit trail for a newly created owner. Runs after the owner has been saved so the
 * assigned id is available. Two things are published to the dedicated {@code AUDIT} logger:
 * <ol>
 * <li>a human-readable line carrying the owner id, the memberId, the registrationDate and the
 * numeric membershipLevel; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} (as JSON) carrying a monotonic sequence
 * number, the owner id, the owner's primary identifier (the memberId) and the membershipLevel.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        int membershipLevel = MembershipLevel.of(owner);
        AUDIT.info(
                "Owner created: id={} memberId={} registrationDate={} membershipLevel={}",
                owner.getId(), owner.getMemberId(), owner.getRegistrationDate(), membershipLevel);

        // The event carries the owner's primary identifier — the unified memberId.
        OwnerCreatedEvent event = OwnerCreatedEvent.of(owner.getId(), owner.getMemberId(), membershipLevel);
        AUDIT.info(event.toJson());
    }
}
