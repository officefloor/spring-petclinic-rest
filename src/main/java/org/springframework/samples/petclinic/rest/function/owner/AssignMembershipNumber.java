package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'}:
 * customerCode is the region-and-hash value assigned by {@link AssignCustomerCode} and YY is the
 * last two digits of the owner's fiscal year (see {@link FiscalYear}), derived from the
 * business-day-adjusted {@code registrationDate} and zero-padded (e.g. {@code 'NSW-1A2B3C4D-M26'}).
 * Runs after {@link AssignCustomerCode} (so customerCode exists) and mutates the built
 * {@link Owner} in place.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", FiscalYear.endYearOf(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
