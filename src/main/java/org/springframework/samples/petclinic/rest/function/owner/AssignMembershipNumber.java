package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipNumber} to the freshly built {@link Owner} before it is saved.
 *
 * <p>The number is formatted {@code '<customerCode>-M<YY>'}, where {@code customerCode} is the
 * code assigned by {@link AssignCustomerCode} and YY is the last two digits of the owner's
 * {@code registrationDate} year (the effective, business-day-adjusted date resolved by
 * {@link ResolveRegistrationDate} and stored by {@link BuildOwner}),
 * e.g. {@code 'SPR-SMI-0007-M26'}. This step therefore runs after both are set.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
