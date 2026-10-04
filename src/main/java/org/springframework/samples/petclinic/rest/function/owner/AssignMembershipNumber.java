package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>}
 * where {@code customerCode} is the owner's already-assigned region-and-hash customer code and
 * {@code YY} is the last two digits of the fiscal year (starting 1 July) that contains the
 * business-day-adjusted {@code registrationDate} (e.g. {@code NSW-1A2B3C4D-M27}).
 *
 * <p>Runs after {@link AssignCustomerCode} and {@link BuildOwner} (which assign the
 * {@code customerCode} and default and business-day-adjust the {@code registrationDate}) and before
 * {@link SaveOwner}, so both inputs are already set on the owner. The fiscal year is derived via
 * {@link FiscalYear}, the single source of truth shared with the read-time {@code fiscalYear} label.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = FiscalYear.yy(owner.getRegistrationDate());
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
