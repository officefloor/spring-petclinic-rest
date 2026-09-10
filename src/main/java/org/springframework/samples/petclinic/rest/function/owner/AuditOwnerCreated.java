package org.springframework.samples.petclinic.rest.function.owner;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.CustomerCode;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;

/**
 * Emits an audit trail entry, via the dedicated {@code AUDIT} logger, for a
 * successfully created owner. The line carries the owner id, the assigned
 * customerCode, the registrationDate, the assigned membershipLevel and the
 * derived membershipNumber.
 *
 * <p>In addition to that human-readable line, an immutable {@link OwnerCreatedEvent} is
 * emitted as a compact JSON object on the same {@code AUDIT} logger, giving the trail a
 * machine-readable record carrying a monotonic {@code seq}, the owner id, the owner's
 * current primary identifier and the membership level.
 */
public class AuditOwnerCreated {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /** Monotonically increasing sequence shared by every owner-created event. */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    public void service(@Val Owner owner) {
        audit.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                MembershipLevels.of(owner), membershipNumber(owner));
        OwnerCreatedEvent event = new OwnerCreatedEvent(SEQUENCE.incrementAndGet(), owner.getId(),
                primaryIdentifier(owner), MembershipLevels.of(owner), OwnerCreatedEvent.OWNER_CREATED);
        audit.info(event.toJson());
    }

    /**
     * The owner's current primary identifier. Today that is the customerCode; when the
     * customerCode is unified into the memberId this returns the memberId instead, so the
     * emitted event always carries whatever identifier is primary at the time.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    private static String membershipNumber(Owner owner) {
        return CustomerCode.membershipNumber(owner.getCustomerCode(), owner.getRegistrationDate());
    }
}
