package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where YY is
 * the two-digit (zero-padded) last two digits of the {@code registrationDate} year (e.g.
 * {@code SYD-SMI-0007-M26}). Runs after {@link AssignOwnerCustomerCode} has stamped the customer code
 * and {@link BuildOwner} has set the registration date, and before {@link SaveOwner} persists the
 * entity, mutating the built owner in place so the saved and returned record carries the number.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format(Locale.ROOT, "%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
