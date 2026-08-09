package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code membershipNumber}, formatted {@code '<customerCode>-M<YY>'} where
 * YY is the last two digits of the {@code registrationDate} year (e.g. 'SYD-SMI-0007-M26').
 *
 * <p>Runs after {@link AssignCustomerCode} (so the customer code exists) and {@link BuildOwner}
 * (so the registration date is set), and before {@link SaveOwner}, mutating the not-yet-persisted
 * owner in place.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner) {
        int yy = owner.getRegistrationDate().getYear() % 100;
        owner.setMembershipNumber(String.format("%s-M%02d", owner.getCustomerCode(), yy));
    }
}
