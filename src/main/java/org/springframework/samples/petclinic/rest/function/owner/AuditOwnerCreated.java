package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits an audit line to the dedicated {@code AUDIT} logger once the owner has been
 * persisted, recording the assigned id, the {@code customerCode}, the
 * {@code registrationDate}, the {@code membershipPoints}, the {@code membershipLevel}
 * and the {@code membershipNumber}.
 */
public class AuditOwnerCreated {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int points = Memberships.membershipPoints(owner, Households.size(owner, ownerRepository));
        AUDIT.info("owner created: id={} customerCode={} registrationDate={} membershipPoints={} "
                + "membershipLevel={} membershipNumber={}",
                owner.getId(), owner.getCustomerCode(), owner.getRegistrationDate(),
                points, Memberships.membershipLevel(points), membershipNumber(owner));
    }

    /**
     * Builds the membership number '<customerCode>-M<YY>', where YY is the last two digits of
     * the registrationDate year (e.g. 'NSW-1A2B3C4D-M26'). Returns {@code null} when either the
     * customer code or the registration date is absent (e.g. legacy seed owners). Mirrors the
     * {@code membershipNumber} exposed on the owner response.
     */
    private static String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }
}
