package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.FiscalYear;

/**
 * Assigns the new owner's {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>}: the owner's customer code, {@code -M}, and the last two
 * digits of the registration date's fiscal year (fiscal year starting 1 July, e.g.
 * {@code NSW-1A2B3C4D-M26}). Runs after {@link AssignOwnerCustomerCode} (so the customer
 * code exists) and after the registration date has been set, and before {@link SaveOwner}
 * persists the number.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = FiscalYear.shortYearOf(owner.getRegistrationDate());
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
