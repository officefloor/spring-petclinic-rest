package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;

/**
 * Assigns the owner's {@code membershipNumber} before it is saved. The number is formatted
 * {@code <customerCode>-M<YY>}, where {@code customerCode} is the owner's customer code and
 * {@code YY} is the last two digits of the fiscal year (starting 1 July) of the
 * business-day-adjusted {@code registrationDate}, zero-padded (e.g. {@code NSW-3F1A9C2B-M27}). Runs
 * after {@link AssignCustomerCode} has set the customer code and {@link BuildOwner} has set the
 * registration date, so both inputs are present. Mutates the entity in place so {@link SaveOwner}
 * persists it.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", FiscalYear.yearOf(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
