package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the new owner's {@code membershipNumber}, formatted
 * {@code <customerCode>-M<YY>}: the owner's customer code, {@code -M}, and the last two
 * digits of the registration date year (e.g. {@code SMI-0007-M26}). Runs after
 * {@link AssignOwnerCustomerCode} (so the customer code exists) and after the registration
 * date has been set, and before {@link SaveOwner} persists the number.
 */
public class AssignOwnerMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
