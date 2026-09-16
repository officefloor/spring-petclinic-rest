package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;

/**
 * Assigns a newly built owner its {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>} where YY is the last two digits of the fiscal year of the
 * owner's registration date (e.g. {@code NSW-1A2B3C4D-M26}; see {@link FiscalYear}). Runs
 * after {@link AssignCustomerCode} and {@link ApplyRegistrationDate} so both inputs are
 * set — the year segment therefore uses the adjusted business day — and before
 * {@link SaveOwner}, mutating the built owner in place so the number is stored and
 * returned with the owner.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = FiscalYear.shortLabel(owner.getRegistrationDate());
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
