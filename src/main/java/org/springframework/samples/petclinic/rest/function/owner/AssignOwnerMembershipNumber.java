package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber} formatted {@code <customerCode>-M<YY>}, where YY is
 * the last two digits of the fiscal year of the business-day-adjusted registrationDate (e.g.
 * {@code NSW-1A2B3C4D-M26}). Runs after the customer code is assigned and the registration date is
 * set.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = FiscalYear.of(owner.getRegistrationDate()) % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
