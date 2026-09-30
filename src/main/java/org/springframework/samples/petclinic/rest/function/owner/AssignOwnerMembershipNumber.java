package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where YY is
 * the two-digit (zero-padded) last two digits of the fiscal year (starting 1 July) of the
 * business-day-adjusted {@code registrationDate} (e.g. {@code NSW-3F9A1C7E-M27}). Runs after
 * {@link AssignOwnerCustomerCode} has stamped the customer code and {@link BuildOwner} has set the
 * business-day-adjusted registration date, and before {@link SaveOwner} persists the entity, mutating
 * the built owner in place so the saved and returned record carries the number.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format(Locale.ROOT, "%02d", Owner.fiscalYear(owner.getRegistrationDate()) % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
