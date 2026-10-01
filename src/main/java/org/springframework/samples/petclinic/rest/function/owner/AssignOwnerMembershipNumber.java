package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>},
 * where {@code customerCode} is the owner's previously assigned region-and-hash customer code and
 * {@code YY} is the last two digits of the owner's fiscal year — the financial year (starting
 * 1 July) of the business-day-adjusted registration date, named by its ending calendar year (e.g.
 * an October 2026 registration falls in fiscal year 2027, giving {@code NSW-1A2B3C4D-M27}).
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", OwnerFiscalYear.fiscalYear(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(String.format("%s-M%s", owner.getCustomerCode(), yy));
    }
}
