package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where YY is
 * the last two digits of the fiscal year (starting 1 July) of the owner's business-day-adjusted
 * {@code registrationDate} (e.g. {@code NSW-1A2B3C4D-M26}). Runs after {@link AssignCustomerCode}
 * and after the registration date has been defaulted and adjusted, so it carries the new
 * region-and-hash {@code customerCode}.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = FiscalYear.yy(owner.getRegistrationDate());
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
