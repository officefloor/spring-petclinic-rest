package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a new owner's {@code membershipNumber}, formatted {@code <customerCode>-M<YY>} where YY is
 * the last two digits of the registration date year (e.g. {@code SYD-SMI-0007-M26}). Runs after
 * {@link AssignOwnerCustomerCode} sets the customer code and {@link DefaultOwnerRegistrationDate}
 * ensures a registration date, and before {@link SaveOwner} persists it, so the value is stored and
 * returned unchanged on later reads of the owner.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        owner.setMembershipNumber(owner.getCustomerCode() + "-M" + yy);
    }
}
