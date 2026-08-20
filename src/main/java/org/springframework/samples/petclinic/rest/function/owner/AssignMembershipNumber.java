package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where YY is
 * the last two digits of the fiscal year (starting 1 July) of the business-day-adjusted
 * {@code registrationDate} (e.g. {@code NSW-1A2B3C4D-M26}). Runs after the customer code has been
 * assigned and the registration date defaulted, so both fields are set.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", FiscalYear.endingYear(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
