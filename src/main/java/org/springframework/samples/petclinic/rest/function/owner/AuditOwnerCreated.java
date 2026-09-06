package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits, once the owner has been persisted, both a human-readable audit line and an immutable
 * structured {@link OwnerCreatedEvent} to the dedicated {@code AUDIT} logger. The line records the
 * assigned id, the {@code customerCode}, the {@code registrationDate}, the {@code membershipPoints},
 * the {@code membershipLevel} and the {@code membershipNumber}; the event carries the sequence, the
 * owner id, the primary identifier and the membership level as JSON.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int points = Memberships.membershipPoints(owner, Households.size(owner, ownerRepository));
        int level = Memberships.cappedLevel(owner, ownerRepository);
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipPoints={} "
                + "membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                points, level, membershipNumber(owner));
        AUDIT.info("{}", OwnerCreatedEvent.next(owner, level).toJson());
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>', where YY is the last two digits of the
     * fiscal year (starting 1 July) containing the registrationDate (e.g. 'NSW-1A2B3C4D-M27').
     * Returns {@code null} when either the customer code or the registration date is absent (e.g.
     * legacy seed owners). Mirrors the {@code membershipNumber} exposed on the owner response.
     */
    private static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(),
                FiscalYears.of(owner.getRegistrationDate()) % 100);
    }
}
