package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Emits the audit trail for a newly created owner. Runs after the owner has been saved so the
 * assigned id is available. Two things are published to the dedicated {@code AUDIT} logger:
 * <ol>
 * <li>a human-readable line carrying the owner id, the customerCode, the registrationDate, the
 * numeric membershipLevel and the membershipNumber; and</li>
 * <li>an immutable structured {@link OwnerCreatedEvent} (as JSON) carrying a monotonic sequence
 * number, the owner id, the owner's current primary identifier and the membershipLevel.</li>
 * </ol>
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner) {
        int membershipLevel = MembershipLevel.of(owner);
        String membershipNumber = String.format("%s-M%02d", owner.getCustomerCode(),
                FiscalYear.yearSegment(owner.getRegistrationDate()));
        AUDIT.info(
                "Owner created: id={} customerCode={} registrationDate={} membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                membershipLevel, membershipNumber);

        // The event carries the owner's CURRENT primary identifier — the customerCode today; when a
        // later checkpoint unifies it into the memberId, feed the memberId here instead.
        OwnerCreatedEvent event = OwnerCreatedEvent.of(owner.getId(), owner.getCustomerCode(), membershipLevel);
        AUDIT.info(event.toJson());
    }
}
