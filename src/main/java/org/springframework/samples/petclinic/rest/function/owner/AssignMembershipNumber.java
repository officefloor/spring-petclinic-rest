package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipNumber} to the freshly built {@link Owner} before it is saved.
 *
 * <p>The number is formatted {@code '<customerCode>-M<YY>'}, where {@code customerCode} is the
 * code assigned by {@link AssignCustomerCode} and YY is the last two digits of the fiscal year
 * (starting 1 July) of the owner's {@code registrationDate} (the effective, business-day-adjusted
 * date resolved by {@link ResolveRegistrationDate} and stored by {@link BuildOwner}),
 * e.g. {@code 'NSW-1A2B3C4D-M26'}. This step therefore runs after both are set.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = org.springframework.samples.petclinic.util.FiscalYears
                .yearSegment(owner.getRegistrationDate());
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
