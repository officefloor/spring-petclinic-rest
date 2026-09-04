package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>}
 * where YY is the last two digits of the fiscal year (1 July basis) of the business-day-adjusted
 * {@code registrationDate} (e.g. {@code NSW-1A2B3C4D-M27}). Runs after the customer code and
 * registration date have been assigned.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", FiscalYear.endingYear(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
