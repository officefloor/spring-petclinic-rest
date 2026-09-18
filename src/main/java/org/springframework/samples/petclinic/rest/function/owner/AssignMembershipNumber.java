package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a newly built owner its {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>}, where YY is the two-digit fiscal year (starting 1 July) of
 * the business-day-adjusted registration date (e.g. {@code NSW-1A2B3C4D-M26}). Runs after
 * both the customer code and the registration date have been assigned, so both inputs are
 * present.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = FiscalYear.twoDigit(owner.getRegistrationDate());
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
