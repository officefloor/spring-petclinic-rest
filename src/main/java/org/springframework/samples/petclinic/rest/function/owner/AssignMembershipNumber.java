package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where
 * {@code <customerCode>} is the owner's already-assigned region-and-hash customer code and
 * {@code YY} is the last two digits of the fiscal year's start-year for the business-day-adjusted
 * {@code registrationDate} (the same {@code YY} as the owner's {@code fiscalYear}, e.g.
 * {@code NSW-1A2B3C4D-M26}).
 *
 * <p>Runs after {@link AssignCustomerCode} (so the customer code exists) and after
 * {@link BuildOwner} (so the registration date is set), and before {@link SaveOwner}.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", FiscalYear.startYear(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
